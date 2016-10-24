package scheduleModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// for eclipse
//import javax.xml.bind.DatatypeConverter;

//for android
import android.util.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParserUtil {

	/**
	 * find a table whose header (thead>th) or if not present, first row (tbody>tr:first) text matches the given array of strings.
	 *  If the header contains less columns than the items in head[], it is sufficient that the first {head.length} columns match
	 * @param doc
	 * @param head
	 * @param exactMatch if true, each item in head[] must exactly match the text in the corresponding column.
	 *                   If false, each item in head[] must be at least contained in the corresponding column
	 * @return
	 */
	public static Element findTableByFirstRow(Document doc, String[] head, boolean exactMatch){
		Element ret=null;
		for(Element table : doc.getElementsByTag("table"))
		{
			Elements tsub=table.children().select(">thead");
			if(!tsub.isEmpty()){
				table=tsub.first();
			}
			else{
				tsub=table.children().select(">tbody");
				if(!tsub.isEmpty()){
					table=tsub.first();
				}
			}

			Elements trs = table.children().select(">tr");

			//table.getElementsByTag("tr");
			if (trs.isEmpty()) {
				continue;
			}
			Element tr = trs.first();

			int i = 0;
			boolean fail = false;

			Elements tdths= tr.getElementsByTag("th");
			tdths.addAll(tr.getElementsByTag("td"));
			if(tdths.size()<head.length){
				fail=true; }
			else {
				for (Element td : tdths) {
					if ((!exactMatch && (td.html().indexOf(head[i]) == -1))
							|| (exactMatch && !(td.text().trim().equals(head[i])))) {
						fail = true;
						continue;
					}
					i++;
					if (i >= head.length) {
						break;
					}
				}
			}
			if (!fail) {
				ret = table;
			}
		}
		return ret;
	}

	public static String tryGetFirstLink(Element e){
		Elements aUrl=e.getElementsByTag("a");
		String url="";
		if(!aUrl.isEmpty()){
			url=aUrl.first().absUrl("href");
		}
		return url;
	}

	public static String stripFirstLast(String s){
		if(s.length()>=2) {
			return s.substring(1,s.length()-1);
		} else {
			return s;
		}
	}

	public static String fixPath(String name) {
		return name.replace("\n", "").replace("\r","").replaceAll("[:\\\\/*?|<>]", "_");
	}

	public static String removeNbsp(String s){
		return s.replaceAll(new Character((char)160).toString(),"");
	}

	public static String text(Element s){
		return text(s.html());
	}
	public static String text(String html){
		String t = Jsoup.parse(html.replaceAll("(?i)<br[^>]*>", "##BR##")).text();
		return removeNbsp(t.replace("##BR##", "\n")).trim();
	}




	public static String format(String template, List<String> v){
		int paramCount=v.size();
		for(int i=0; i<paramCount; i++){
			template=template.replace("{"+Integer.toString(i)+"}", v.get(i));
		}
		return template;
	}
	public static String format(String template, String[] v){
		int paramCount=v.length;
		for(int i=0; i<paramCount; i++){
			template=template.replace("{"+Integer.toString(i)+"}", v[i]);
		}
		return template;
	}





	public static byte[] serialize(Object x){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] arr=null;

		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(x);
			arr = bos.toByteArray();
		}
		catch(IOException ex){
			ex.printStackTrace();}
		finally {
			try {if (out != null) {out.close();}} catch (IOException ex) {}
			try {bos.close();} catch (IOException ex) {}
		}
		return arr;
	}

	public static Object unserialize(byte[] arr){
		ByteArrayInputStream bis = new ByteArrayInputStream(arr);
		ObjectInput in = null;
		Object o=null;
		try {
			in = new ObjectInputStream(bis);
			o = in.readObject();
		}
		catch(IOException ex){}
		catch(ClassNotFoundException ex){}
		finally {
			try {bis.close();} catch (IOException ex) {}
			try {if (in != null) {in.close();}} catch (IOException ex) {}
		}
		return o;
	}






	public static String toBase64(byte[] arr){
		return Base64.decode(arr,Base64.DEFAULT).toString();
        // for eclipse
		//return DatatypeConverter.printBase64Binary(arr);
	}
	public static byte[] fromBase64(String x){
		return Base64.encode(x.getBytes(),Base64.DEFAULT);
        // for eclipse
		//return DatatypeConverter.parseBase64Binary(x);
	}





















	public static String stringOrEmpty(String o){
		if(o==null) {
			return "";
		}
		return o;
	}
	public static String join(Collection<?> col, String delim) {
		StringBuilder sb = new StringBuilder();
		Iterator<?> iter = col.iterator();
		if (iter.hasNext()) {
			sb.append(iter.next().toString());
		}
		while (iter.hasNext()) {
			sb.append(delim);
			sb.append(iter.next().toString());
		}
		return sb.toString();
	}
	public static HashMap<String,String> parseUrlQuery(String u){
		URL url=null;
		try{
			url=new URL(u);
		}
		catch(Exception e){
			return null;
		}
		String q=url.getQuery();
		if(q==null) {
			return null;
		}
		HashMap<String, String> ret=new HashMap<String,String>();
		String[] qe=q.split("&");
		for(String query : qe){
			String v[]=query.split("=");
			for(int k=0; k<v.length; k++){
				try{v[k]=URLDecoder.decode(v[k],"UTF-8");}catch(Exception e){}
			}

			if(v.length==1){
				ret.put(v[0], "");
			}
			else if(v.length==2){
				ret.put(v[0], v[1]);
			}
		}
		return ret;
	}

	public static List<String> extractText(Element node){
		List<String> ret=new ArrayList<String>();
		/*def extract_text(node):
        arr=[]
        for child in node.findall(".//"):
            if child.tag.lower() in ["a","span"] and len(child.text)>0:
                arr.append(child.text.replace('\xa0',' '))
        #print(arr)
        return arr
		 */
		Elements elements=new Elements();
		elements.addAll(node.select("a"));
		elements.addAll(node.select("span"));
		elements.addAll(node.select("li"));
		for(Element e : elements){
			String owntext=removeNbsp(e.ownText());
			if(!owntext.replaceAll(" ","").isEmpty()){
				ret.add(removeNbsp(owntext));
			}
		}
		return ret;
	}

	public static List<String> findStringsInScript(Document doc, String contains){
		List<String> ret=new LinkedList<String>();
		Pattern p=Pattern.compile("\"(.+?)\"");
		for(Element script : doc.getElementsByTag("script")){
			String html=script.html(); //.replace("\n", "").replace("\r","");
			Matcher m=p.matcher(html);
			while(m.find()){
				String text=m.group(1);
				if(text.indexOf(contains)!=-1){
					ret.add(text);
				}
			}
		}
		return ret;
	}

	public static Element findParentByTag(Element node, String tagname, int limit){
		if(node==null) {
			return null;
		}
		for(int k=0; k<limit; k++){
			node=node.parent();
			if(node==null) {
				break;
			}
			if(node.tagName()==tagname) {
				break;
			}
		}
		return node;
	}

	public static int tryParseInt(String s, int i){
		try{
			i=Integer.parseInt(s);
		}
		catch(Exception e){}
		return i;
	}




}
