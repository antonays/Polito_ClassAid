package scheduleModule;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SessionUtil {

    /**
     * Submit a form
     * @param s
     * @param form the &lt;form&gt; element
     * @param referer
     * @param additionalData additional fields to send.
     * @return a com.example.anton.classaidtrialslev1.Response object
     * @throws java.io.IOException
     * @throws ScheduleGetException
     */
    public static Response sendForm(SessionLike s, Element form, String referer, Map<String,String> additionalData)
            throws IOException, ScheduleGetException {

        Map<String,String> data=new HashMap<String,String>();
        String url=form.absUrl("action");
        String method=form.attr("method");
        method=method.toLowerCase();
        if(method.isEmpty() ||
                (!method.equals("post") && !method.equals("get"))
                )
            method="post";

        if(url.isEmpty())throw new ScheduleGetException("form url is empty");
        for(Element e : form.getElementsByTag("input")){
            String name=e.attr("name");
            String value=e.attr("value");
            if (!name.isEmpty() && !value.isEmpty()){
                data.put(name,value);
            }
        }
        if(additionalData!=null)
            data.putAll(additionalData);

        return s.request(url, method, referer, data);
    }


    /**
     * Send the first form in the document if onload contains 'submit()'
     * @param s
     * @param doc
     * @param referer
     * @param additionalData same as sendForm
     * @return a com.example.anton.classaidtrialslev1.Response object
     * @throws java.io.IOException
     * @throws ScheduleGetException
     */
    public static Response postFirstForm(SessionLike s, Document doc, String referer, Map<String,String> additionalData)
            throws IOException, ScheduleGetException {

        Element body=doc.body();
        String onload=body.attr("onload");

        if(onload.indexOf("submit()")==-1)
            throw new ScheduleGetException("No submit() in onload");

        Elements forms=body.getElementsByTag("form");

        if(forms.isEmpty())
            throw new ScheduleGetException("No forms in the document");
        Element form=forms.first();

        return sendForm(s,form,referer,additionalData);
    }

    /**
     * Dump a CookieStore to JSON
     * @param x
     * @return
     */
    public static JSONObject cookieStoreToJSON(CookieStore x){
        JSONObject j=new JSONObject();
        JSONArray a=new JSONArray();
        for(Cookie c : x.getCookies()){
            JSONObject y=new JSONObject();

            try {
                String name = c.getName();
                if (name != null) y.put("nam", name);

                String value = c.getValue();
                if (value != null) y.put("val", value);

                String comment = c.getComment();
                if (comment != null) y.put("com", comment);

                String commentUrl = c.getCommentURL();
                if (commentUrl != null) y.put("cou", commentUrl);

                String domain = c.getDomain();
                if (domain != null) y.put("dom", domain);

                String path = c.getPath();
                if (path != null) y.put("pat", path);

                int version = c.getVersion();
                if (version > 0) y.put("ver", new Integer(value));

                y.put("sec", new Boolean(c.isSecure()));

                Date expiry = c.getExpiryDate();
                if (expiry != null) y.put("exp", new Long(expiry.getTime()));
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            a.put(y);
        }
        try {
            j.put("cookies", a);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }

    /**
     * Create a new CookieStore from JSON
     * @param j
     * @return
     */
    public static CookieStore cookieStoreFromJSON(JSONObject j){

        BasicCookieStore x= new BasicCookieStore();
        if(j==null)return x;
        JSONArray a=j.optJSONArray("cookies");
        if(a==null)return x;

        for(int i=0; i<a.length(); i++){
            JSONObject y=a.optJSONObject(i);
            if(y==null)continue;

            String name=y.optString("nam",null);
            String value=y.optString("val",null);
            if(name==null || value==null)continue;

            BasicClientCookie c=new BasicClientCookie(name,value);

            String comment=y.optString("com",null);
            if(comment!=null)c.setComment(comment);

            String commentUrl=y.optString("cou",null);
            if(commentUrl!=null)c.setAttribute(BasicClientCookie.COMMENTURL_ATTR,commentUrl);

            String domain=y.optString("dom",null);
            if(domain!=null)c.setDomain(domain);

            String path=y.optString("pat",null);
            if(path!=null)c.setPath(path);

            int version=y.optInt("ver",0);
            if(version>0)c.setVersion(version);

            c.setSecure(y.optBoolean("sec",false));

            try{
                Date expiry=new Date(y.getLong("exp"));
                c.setExpiryDate(expiry);
            }catch(JSONException e){}

            x.addCookie(c);
        }

        return x;
    }
}
