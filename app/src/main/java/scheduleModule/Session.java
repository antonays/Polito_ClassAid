package scheduleModule;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Session implements SessionLike{

    private final static boolean LOGGING=true;
    private final Semaphore mutex=new Semaphore(1,true);

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    private String userAgent= "Mozilla/5.0 (X11; Linux x86_64; rv:30.0) Gecko/20100101 Firefox/30.0";
    private int timeout=10000;
    DefaultHttpClient http;
    CookieStore cookiestore;
    HttpContext ctx;
    private HttpResponse lastResponse=null;

    public void setContext(HttpContext ctx){
        this.ctx=ctx;
    }
    public void setCookieStore(CookieStore cookiestore){
        this.cookiestore=cookiestore;
        ctx.setAttribute(ClientContext.COOKIE_STORE, this.cookiestore);
    }

    public JSONObject dumpCookies(){
        JSONObject ret=SessionUtil.cookieStoreToJSON(this.cookiestore);
        if(ret==null){
            try {
                ret=new JSONObject().put("cookies",new JSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    public void loadCookies(JSONObject ck){
        CookieStore cs=SessionUtil.cookieStoreFromJSON(ck);
        setCookieStore(cs);
    }



    private void initialize(){
        //System.setProperty("javax.net.ssl.trustStore", "polito.jks");
        http=new DefaultHttpClient();
        http.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

        // Custom Redirect handler to fix malformed redirect headers from polito.it
        http.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public URI getLocationURI(HttpResponse response,
                                      HttpContext context) throws ProtocolException {
                Header h=response.getLastHeader("Location");
                if(h!=null){
                    String url=h.getValue();
                    //URL-encode invalid character '|'
                    if(url.indexOf("|")!=-1){
                        url=url.replaceAll("\\|","%7C");
                        response.removeHeaders("Location");
                        response.setHeader("Location",url);
                    }
                    //System.out.println("REDIRECT "+url);
                }

                return super.getLocationURI(response, context);
            }
        });

        cookiestore=new BasicCookieStore();
        ctx = new BasicHttpContext();
        ctx.setAttribute(ClientContext.COOKIE_STORE, cookiestore);

        http.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.BROWSER_COMPATIBILITY);

        setTimeout(timeout);
    }

    public Session()
    {
        initialize();
    }

    public HttpResponse httpRequest(String url, String method, String referer, Map<String,String> data) throws IOException{
        if(mutex.availablePermits()<1){
            System.out.println("[com.example.anton.classaidtrialslev1.Session] WARNING: You are trying to make two simultaneous connections!");
        }
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            HttpUriRequest req;
            if (method.equalsIgnoreCase("get")) {
                HttpGet x = new HttpGet(url);
                req = x;
                if (data != null) {

                    StringBuffer urlData = new StringBuffer();
                    boolean first = true;
                    for (Map.Entry<String, String> e : data.entrySet()) {
                        if (first)
                            first = false;
                        else
                            urlData.append("&");
                        urlData.append(URLEncoder.encode(e.getKey(), "UTF-8")
                                + "=" + URLEncoder.encode(e.getValue(), "UTF-8"));

                    }
                    URL urlObj = new URL(url);
                    if (urlObj.getQuery() == null) {
                        url += "?" + urlData.toString();
                    } else {
                        url += "&" + urlData.toString();
                    }
                }
            } else if (method.equalsIgnoreCase("post")) {
                HttpPost x = new HttpPost(url);
                req = x;

                if (data != null) {
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();

                    for (Map.Entry<String, String> e : data.entrySet()) {
                        nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
                    }
                    x.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
                }
            } else throw new IOException();

            req.setHeader("User-Agent", userAgent);
            if (referer != null)
                req.setHeader("Referer", referer);

            if (LOGGING) {
                HashMap<String,String> datacp=null;
                if(data!=null) {
                    datacp = new HashMap<String, String>(data);
                    if(datacp.containsKey("j_username"))datacp.put("j_username","#username#");
                    if(datacp.containsKey("j_password"))datacp.put("j_password","#password#");
                }
                System.out.println(method.toUpperCase() + " " + url
                                + (referer != null ? " (ref:" + referer + ")" : "")
                                + (datacp != null ? " (data:" + datacp.toString() + ")" : "")
                );
            }

            HttpResponse response = null;
            try {
                if (lastResponse != null && lastResponse.getEntity() != null)
                    lastResponse.getEntity().consumeContent();
                //EntityUtils.consumeQuietly(lastResponse.getEntity());

                response = http.execute(req, ctx);

            } catch (ClientProtocolException e) {
                //e.printStackTrace();
                throw new IOException();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            lastResponse = response;
            mutex.release();
            return response;
        }
        catch(IOException e){
            mutex.release();
            throw e;
        }

    }

    public Response request(String url, String method, String referer, Map<String,String> data) throws IOException {
        HttpResponse response=httpRequest(url,method,referer,data);
        return new Response(response,ctx);
    }


    public Response get(String url, String referer) throws IOException {
        return request(url,"get", referer, null);
    }
    public Response get(String url) throws IOException {
        return request(url,"get", null, null);
    }


    public CookieStore getCookieStore(){
        return cookiestore;
    }
    public void setTimeout(int timeout){
        this.timeout=timeout;
        HttpParams params = http.getParams();
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
    }
    /**
     * Sends a form
     */

    public Response sendForm(Element form, String referer, Map<String,String> additionalData)
            throws IOException, ScheduleGetException {

        Map<String,String> data=new HashMap<String,String>();
        String url=form.absUrl("action");
        String method=form.attr("method");
        method=method.toLowerCase();
        if(method.isEmpty() ||
                (!method.equals("post") && !method.equals("get"))
                )
            method="post";

        //System.out.println(url);
        if(url.isEmpty())throw new ScheduleGetException();
        for(Element e : form.getElementsByTag("input")){
            String name=e.attr("name");
            String value=e.attr("value");
            if (!name.isEmpty() && !value.isEmpty()){
                data.put(name,value);
                //System.out.println(name+":"+value);
            }
        }
        if(additionalData!=null)
            data.putAll(additionalData);

        return request(url, method, referer, data);
    }
    /**
     * Sends the first form of a document
     */
    public Response postFirstForm(Document doc, String referer, Map<String,String> additionalData)
            throws IOException, ScheduleGetException {

        Element body=doc.body();
        String onload=body.attr("onload");

        if(onload.indexOf("submit()")==-1)
            throw new ScheduleGetException();

        Elements forms=body.getElementsByTag("form");

        if(forms.isEmpty())
            throw new ScheduleGetException();
        Element form=forms.first();

        return sendForm(form,referer,additionalData);
    }

    Session fork(){
        Session ns=new Session();
        ns.loadCookies(dumpCookies());
        return ns;
    }
}