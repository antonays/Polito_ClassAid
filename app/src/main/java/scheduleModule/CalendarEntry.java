package scheduleModule;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class CalendarEntry implements Serializable{
	private GregorianCalendar start, end;
	private String title, text;
	private String name;
	private String teacher;
	private String room;
	private String range;
    private int dayOfWeek;
    private Double lengthTag;


	public CalendarEntry(GregorianCalendar date, String text){
		this(date,null,null,text);
	}

	// standart constructor - to get from server
	public CalendarEntry(GregorianCalendar start, GregorianCalendar end, String title, String text){
		this.start=start;
		this.end=end;
        this.title=title;
        this.text=text;
		String[] splitted = cutText(text);
		this.name = splitted[0];
		this.teacher = splitted[1];
		this.room = splitted[3];
		this.range = splitted[2];
        this.dayOfWeek = start.DAY_OF_WEEK;
	}

	// file constructor - to load file from text file as stream of strings
	public CalendarEntry(String str)
	{
		String[] splitted = str.split("@");
		this.start = stringToDate(splitted[0]);
		this.end = stringToDate(splitted[1]);
		this.name = splitted[2];
		this.teacher = splitted[3];
		this.room = splitted[4];
		this.range = splitted[5];
        this.dayOfWeek = start.get(Calendar.DAY_OF_WEEK);
	}
	public String getName(){
		return this.name;
	}
	public String getTeacher(){
		return this.teacher;
	}
	public String getRoom(){
		return this.room;
	}
	public String getStartTimeDate(){
		return dateToStringT(this.start);
	}
	public String getEndTimeDate(){
		return dateToStringT(this.end);
	}
    public Integer getWeekDay(){return dayOfWeek;}
	public GregorianCalendar getStart(){
		return this.start;
	}
	public GregorianCalendar getEnd()
	{
		return this.end;
	}

	private String[] cutText(String text)
	{
		String[] splitted = ParserUtil.text(text).replace("\n", ",").split(",");
		return splitted;
	}

	public static GregorianCalendar stringToDate(String str){

		GregorianCalendar c=null;
		str=str.trim();
		//DD-MM-YYYY HH:MM:SS
		if(c==null) {
			c=tryParse(str,
					"([0-9]{2})/([0-9]{2})/([0-9]{4})[ ]([0-9]{2}):([0-9]{2}):([0-9]{2})",
					new int[]{
					GregorianCalendar.DAY_OF_MONTH, GregorianCalendar.MONTH, GregorianCalendar.YEAR,
					GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND
			},false);
		}
		//DD-MM-YYYYTHH:MM:SS
		if(c==null) {
			c=tryParse(str,
					"([0-9]{2})/([0-9]{2})/([0-9]{4})[T]([0-9]{2}):([0-9]{2}):([0-9]{2})",
					new int[]{
					GregorianCalendar.DAY_OF_MONTH, GregorianCalendar.MONTH, GregorianCalendar.YEAR,
					GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND
			},false);
		}

		// YYYY-MM-DD hh:mm:ss

		if(c==null) {
			c=tryParse(str,
					"([0-9]{4})-([0-9]{2})-([0-9]{2})[ ]([0-9]{2}):([0-9]{2}):([0-9]{2})",
					new int[]{
					GregorianCalendar.YEAR, GregorianCalendar.MONTH, GregorianCalendar.DAY_OF_MONTH,
					GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND
			},false);
		}
		// YYYY-MM-DDThh:mm:ss  <-- as it comes from the server
		if(c==null) {
			c=tryParse(str,
					"([0-9]{4})-([0-9]{2})-([0-9]{2})[T]([0-9]{2}):([0-9]{2}):([0-9]{2})",
					new int[]{
					GregorianCalendar.YEAR, GregorianCalendar.MONTH, GregorianCalendar.DAY_OF_MONTH,
					GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND
			},false);
		}

		// YYYY-MM-DD
		if(c==null) {
			c=tryParse(str,
					"([0-9]{4})-([0-9]{2})-([0-9]{2})",
					new int[]{
					GregorianCalendar.YEAR, GregorianCalendar.MONTH, GregorianCalendar.DAY_OF_MONTH
			},false);
		}
		// DD/MM/YYYY
		if(c==null) {
			c=tryParse(str,
					"([0-9]{2})/([0-9]{2})/([0-9]{4})",
					new int[]{
					GregorianCalendar.DAY_OF_MONTH, GregorianCalendar.MONTH, GregorianCalendar.YEAR
			},true);
		}
		// DD/MM/YY
		if(c==null){
			c=tryParse(str,
					"([0-9]{2})/([0-9]{2})/([0-9]{2})",
					new int[]{
					GregorianCalendar.YEAR, GregorianCalendar.MONTH, GregorianCalendar.DAY_OF_MONTH
			},true);
			if(c!=null) {
				c.set(GregorianCalendar.YEAR, c.get(GregorianCalendar.YEAR)+2000);
			}
		}

		return c;
	}

	private static GregorianCalendar tryParse(String str, String regex, int[] fields, boolean reverse){
		Pattern p=Pattern.compile(regex);
		Matcher m=p.matcher(str);
		if(m.matches()){
			GregorianCalendar c=new GregorianCalendar();
			c.set(GregorianCalendar.HOUR_OF_DAY, 0);
			c.set(GregorianCalendar.MINUTE, 0);
			c.set(GregorianCalendar.SECOND, 0);

			int beg, end, add;
			if(!reverse){beg=0;end=fields.length;add=1;}
			else{ beg=fields.length-1; end=-1; add=-1; }

			for(int i=beg; i!=end; i+=add){
				int val=Integer.parseInt(m.group(i+1));
				if(fields[i]==GregorianCalendar.MONTH) {
					val--;
				}
				c.set(fields[i],val);
			}

			return c;
		} else {
			return null;
		}
	}
	/**
	 * DD/MM/YYYY
	 * @param date
	 * @return
	 */
	public static String dateToString(GregorianCalendar date){
		if(date == null) {
			return "";
		}
		return (new SimpleDateFormat("dd/MM/yyyy")).format(date.getTime());
	}
	/**
	 * DD/MM/YYYY hh:mm:ss
	 * @param date
	 * @return
	 */
	public static String dateToStringT(GregorianCalendar date){
		if(date == null) {
			return "";
		}
		return (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(date.getTime());
	}

	/**
	 * Date to string: YYYY-MM-DD hh:mm:ss
	 * @param date
	 * @return
	 */
	public static String dateToStringSQL(GregorianCalendar date){
		if(date == null) {
			return "";
		}
		return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date.getTime());
	}

	/**
	 * For debug purposes
	 * @return
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Start: " + CalendarEntry.dateToStringT(start) + "\n" + "End: " + CalendarEntry.dateToStringT(end) + "\n");
		sb.append("Name: " + this.name + "\n" + "Prof: " + this.teacher + "\n" + "Room: " + this.room +"\n" + this.range + "\n");
		return sb.toString();
	}

	public String stringToFile()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(CalendarEntry.dateToStringT(start)+"@");
		sb.append(CalendarEntry.dateToStringT(end)+"@");
		sb.append(this.name+"@");
		sb.append(this.teacher+"@");
		sb.append(this.room+"@");
		sb.append(this.range+"@");
		return sb.toString();
	}

	public JSONObject toJSON(){
		JSONObject j=new JSONObject();
		try {
			if(this.start!=null) {
				j.put("start", dateToStringSQL(this.start));
			}
			if(this.end!=null) {
				j.put("end", dateToStringSQL(this.end));
			}
			if(this.title!=null) {
				j.put("title", this.title);
			}
			if(this.text!=null) {
				j.put("text", this.text);
			}
		}catch(JSONException e){}
		return j;
	}
	public void fromJSON(JSONObject j) throws JSONException{
		String start=null,end=null,text=null,title=null;
		GregorianCalendar gstart=null,gend=null;

		start = j.optString("start",null);
		if(start!=null) {
			gstart=stringToDate(start);
		}
		end = j.optString("end",null);
		if(end!=null) {
			gend=stringToDate(end);
		}
		text=j.optString("text",null);
		title=j.optString("title",null);

		this.start=gstart;
		this.end=gend;
		this.title=title;
		this.text=text;
	}

	@Override
	public boolean equals(Object arg0) {
		CalendarEntry arg = (CalendarEntry)arg0;
		if (this.getStartTimeDate().equals(arg.getStartTimeDate())){

			if (this.getEndTimeDate().equals(arg.getEndTimeDate()))
			{
				if (this.getName().equals(arg.getName()))
				{
					if (this.getTeacher().equals(arg.getTeacher()))
					{
						if (this.getRoom().equals(arg.getRoom())){
							return true;
						}
						return false;
					}
					return false;

				}
				return false;

			}
			return false;

		}
		else{ return false; }
	}

	@Override
	public int hashCode()
	{
		int startHash = start.DAY_OF_MONTH+start.MONTH+start.YEAR+start.HOUR+start.MINUTE;
		int endHash = end.DAY_OF_MONTH+end.MONTH+end.YEAR+end.HOUR+end.MINUTE;
		int hashcode =startHash*endHash+this.getName().hashCode()+this.getTeacher().hashCode()+this.getRoom().hashCode();
		/*		if (this.getTeacher().equals("CORNO FULVIO"))
		{


			int x=0;
			System.out.println(this.toString());
			System.out.println("start hash" + this.start.hashCode());
			System.out.println("start hash getter" + this.getStart().hashCode());
			System.out.println("start string hash" + dateToStringT(start).hashCode());
			System.out.println("end hash" + this.start.hashCode());
			System.out.println("end hash getter" + this.getEnd().hashCode());
			System.out.println("end string hash" + dateToStringT(end).hashCode());
			System.out.println("name hash" + this.name.hashCode());
			System.out.println("name getter hash" + this.getName().hashCode());
			System.out.println("teacher hash" + this.teacher.hashCode());
			System.out.println("teacher getter hash" + this.getTeacher().hashCode());
			System.out.println("room hash" + this.room.hashCode());
			System.out.println("room getter hash" + this.getRoom().hashCode());
			System.out.println("total hash" + hashcode);
		}*/
		//int startHash = start.DAY_OF_MONTH+start.MONTH+start.YEAR+start.HOUR+start.MINUTE;
		//int endHash = end.DAY_OF_MONTH+end.MONTH+end.YEAR+end.HOUR+end.MINUTE;
		//int hashcode =startHash+endHash+this.getName().hashCode()+this.getTeacher().hashCode()+this.getRoom().hashCode();
		return hashcode;
	}
}
