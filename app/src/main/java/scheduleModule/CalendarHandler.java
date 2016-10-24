package scheduleModule;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CalendarHandler {

	HashMap<String,String> fieldNames;
	HashMap<String,String> fieldFreeNames;
	HashMap<String,HashMap<String,String>> fields;

	private static final String URL_FILTERED_ORARI="http://www.swas.polito.it/dotnet/orari_lezione_pub/filtri_consultazione_generale.aspx";
	public static final String ANNO_ACCADEMICO="annoaccademico", SEDE="sede", LAUREA="laurea", AREA="area", CORSO="corso";
	public static final String ANNO="anno", ORIENTAMENTO="orientamento", INIZIALI="iniziali";

	String url;
	String urlNext;

	String methodNext;

	int step;

	SessionLike s;
	//Jsoup Objects
	Document doc;
	Element form;
	FormSender fs;

	public CalendarHandler() throws IOException, ScheduleGetException {
		step=0;
		url=URL_FILTERED_ORARI;

		fieldNames = new HashMap<String,String>();
		fieldNames.put(ANNO_ACCADEMICO, "ctl00$Pagina$ddlAnnoAccademico");
		fieldNames.put(SEDE, "ctl00$Pagina$ddlSede");
		fieldNames.put(LAUREA, "ctl00$Pagina$ddlTipoLaurea");
		fieldNames.put(AREA, "ctl00$Pagina$ddlArea");
		fieldNames.put(CORSO, "ctl00$Pagina$ddlCdl");
		fieldNames.put(ANNO,"ctl00$Pagina$ddlAnno");
		fieldNames.put(ORIENTAMENTO, "ctl00$Pagina$ddlOrientamenti");

		fieldFreeNames=new HashMap<String,String>();
		fieldFreeNames.put(INIZIALI, "ctl00$Pagina$tbxInizCognome");

		fields= new HashMap<String,HashMap<String,String>>();
		for(String key : fieldNames.keySet()){
			fields.put(key, new HashMap<String,String>());
		}

		s=new Session();
		fs=new FormSender();
		Response r=s.get(URL_FILTERED_ORARI);

		doc=r.parse();
		refreshFields();
	}

	public CalendarHandler(String url, SessionLike s) throws IOException, ScheduleGetException {
		step=2;
		Response r=s.get(url);
		doc=r.parse();
	}

	public List<List<String>> getOptionsFromFormSender(){
		return fs.getOptions();
	}

	public void setDate(GregorianCalendar date) throws IOException{
		String STATE_TEMPLATE="{\"startDate\":\"{startDate}\",\"selectionStart\":\"{selectionStart}\",\"selectionEnd\":\"{selectionEnd}\"}";
		String EVTARG_TEMPLATE="JSON{\"action\":\"TimeRangeSelected\",\"parameters\":{\"start\":\"{selectionStart}\",\"end\":\"{selectionEnd}\"},\"header\":{state}}";

		date.set(GregorianCalendar.HOUR_OF_DAY, 0);
		date.set(GregorianCalendar.MINUTE, 0);
		date.set(GregorianCalendar.SECOND, 0);

		while( date.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY){
			date.add(GregorianCalendar.DAY_OF_MONTH, -1);
		}
		GregorianCalendar startDate=(GregorianCalendar)date.clone();
		date.add(GregorianCalendar.DAY_OF_MONTH, 1);
		GregorianCalendar selectionStart=(GregorianCalendar)date.clone();
		date.add(GregorianCalendar.DAY_OF_MONTH, 7);
		GregorianCalendar selectionEnd=(GregorianCalendar)date.clone();

		String state=STATE_TEMPLATE
				.replace("{startDate}", dateToString(startDate))
				.replace("{selectionStart}", dateToString(selectionStart))
				.replace("{selectionEnd}", dateToString(selectionEnd));
		String evtarg=EVTARG_TEMPLATE
				.replace("{selectionStart}", dateToString(selectionStart))
				.replace("{selectionEnd}", dateToString(selectionEnd))
				.replace("{state}", state);
	}

	private String dateToString(GregorianCalendar date){
		String d=new SimpleDateFormat("yyyy-MM-dd").format(date.getTime());
		String t=new SimpleDateFormat("HH:mm:ss").format(date.getTime());
		return d+"T"+t;
	}

	private void refreshFields() throws ScheduleGetException {
		if(doc==null) {
			return;
		}

		form=doc.getElementById("form1");
		if(form==null) {
			throw new ScheduleGetException();
		}

		urlNext=form.absUrl("action");
		methodNext=form.attr("method").toLowerCase();
		if(!methodNext.equals("post") && !methodNext.equals("get")) {
			methodNext="post";
		}

		fs.clear();
		fs.putFromForm(form);

		for(HashMap<String,String> m : fields.values()){
			m.clear();
		}

		for(Map.Entry<String,String> x : fieldNames.entrySet()){
			Elements selects=form.getElementsByAttributeValue("name", x.getValue());
			if(selects.isEmpty())
			{
				continue;//throw new com.example.anton.classaidtrialslev1.PoliParsingException();
			}
			selects=selects.select("select");
			if(selects.isEmpty()) {
				throw new ScheduleGetException();
			}
			Element select=selects.first();

			HashMap<String,String> optList=new HashMap<String,String>();

			for(Element opt : select.getElementsByTag("option")){
				optList.put(opt.attr("value"),ParserUtil.text(opt));
			}

			fields.put(x.getKey(), optList);
		}

	}

	public boolean setFieldDescriptionContains(String field, String str) throws IOException, ScheduleGetException {
		String choice=null;
		str=str.toLowerCase();
		int descSize=Integer.MAX_VALUE;
		for(Map.Entry<String,String> entry : getFieldChoices(field).entrySet()){
			String value=entry.getValue();
			if(value.toLowerCase().indexOf(str)!=-1 && value.length()<descSize){
				choice=entry.getKey();
				descSize=value.length();
			}
		}
		if(choice==null) {
			return false;
		}
		setField(field,choice);
		return true;
	}
	public void setField(String field, String choice) throws IOException, ScheduleGetException {
		if(step>1) {
			return;
		}
		String fieldName;

		fieldName=fieldFreeNames.get(field);
		if(fieldName!=null){
			realSetFreeField(fieldName,choice);
			return;
		}

		fieldName=fieldNames.get(field);
		HashMap<String,String> fieldValues=fields.get(field);
		if(fieldName==null || fieldValues==null) {
			return;
		}
		if(fieldValues.get(choice)==null) {
			return;
		}

		String prev=fs.get(fieldName);
		if(prev!=null && prev.equals(choice)) {
			return;
		}

		realSetField(fieldName, choice);
	}

	public HashMap<String,String> getFieldChoices(String field){
		return fields.get(field);
	}

	private void realSetField(String name, String choice) throws IOException, ScheduleGetException {


		fs.put(name, choice);
		if(step>0) {
			fs.put("__EVENTTARGET", name);
		}
		Response r =fs.sendForm(s,urlNext, methodNext, url);

		doc=r.parse();
		refreshFields();

	}

	private void realSetFreeField(String name, String choice) throws IOException, ScheduleGetException {

		fs.put(name, choice);
		if(step>0) {
			fs.put("__EVENTTARGET", "ctl00$Pagina$lb_Aggiorna");
		}
		Response r =fs.sendForm(s,urlNext, methodNext, url);

		doc=r.parse();
		refreshFields();
	}
	public void next() throws IOException, ScheduleGetException {
		if(step>0) {
			return;
		}

		realSetField("__EVENTTARGET","ctl00$Pagina$lb_GoToCalendar");

		//System.out.println(url);
		//System.out.println(urlNext);
		step=1;
		//url=URL_ORARI_CONSULTAZIONE;
	}

	List<CalendarEntry> orariCached=null;
	boolean caching=false;

	public List<CalendarEntry> schedule() throws IOException, ScheduleGetException {
		if(caching && orariCached!=null) {
			return orariCached;
		}
		if(step<1) {
			return null;
		}


		List<CalendarEntry> ret=new LinkedList<CalendarEntry>();

		//String html=doc.html();

		String events=null;
		Pattern p=Pattern.compile("v.events\\s*=\\s*(\\[.+\\])\\s*;\\s*v.hours");
		for(Element script : doc.getElementsByTag("script")){
			String html=script.html().replace("\n", "").replace("\r","");
			Matcher m=p.matcher(html);
			if(m.find()){
				events=m.group(1);
				break;
			}
		}
		if(events==null) {
			//System.out.println(doc.html());
			throw new ScheduleGetException();

		}


		events=events.replace("\\'","'");

		JSONArray arr=null;
		try {
			arr = new JSONArray(events);
		}catch(JSONException ex){
			throw new ScheduleGetException();
		}

		for(int i=0; i<arr.length(); i++){
			try {
				JSONObject x = arr.getJSONObject(i);

				String start = x.getString("Start"),
						end = x.getString("End"),
						text = x.getString("Text");
				if (start == null || end == null || text == null) {
					continue;
				}
				CalendarEntry ce = new CalendarEntry(
						CalendarEntry.stringToDate(start),
						CalendarEntry.stringToDate(end),
						null,
						text);
				ret.add(ce);
			}catch(JSONException ex){
				ex.printStackTrace();
			}
		}
		if(caching){
			orariCached=ret;
		}
		return ret;
	}
}