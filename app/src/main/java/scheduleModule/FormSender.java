package scheduleModule;


import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

/**
 * Builds and sends an HTML form (POST or GET, urlencoded)
 */
public class FormSender{

    private HashMap<String,String> data=new HashMap<String,String>();
    private List<List<String>> options = new LinkedList<>();

    public void clear(){
        data.clear();
    }

    public List<List<String>> getOptions()
    {
        return options;
    }
    /**
     * parse an HTML form tag and add all its elements
     * @param form
     */
    public void putFromForm(Element form){
        for(Element input : form.getElementsByTag("input")){
            String name=input.attr("name");
            String value=input.attr("value");
            String type=input.attr("type");
            if(name.isEmpty())continue;
            if(!input.hasAttr("value"))continue;
            if( type.equalsIgnoreCase("submit") ||
                    type.equalsIgnoreCase("button")
                    )continue;
            data.put(name, value);
        }
        for(Element textarea : form.getElementsByTag("textarea")){
            String name=textarea.attr("name");
            if(name.isEmpty())continue;
            String value=textarea.text();
            data.put(name, value);

        }
        for(Element select : form.getElementsByTag("select")){
            int flag=0;
            String name=select.attr("name");
            if(name.isEmpty())continue;
            String value=null;
            List<String> optionsField = new LinkedList<>();
            for(Element option : select.getElementsByTag("option")){
                optionsField.add(option.text());
                if(option.hasAttr("selected") && flag==0 ){
                    value=option.attr("value");
                    flag=1;
                    //break;
                }
                if (!optionsField.contains(option.text())) {
                    optionsField.add(option.text());
                }
            }
            if (select.attr("id").equals("Pagina_ddlCdl")) {
                options.add(optionsField);
            }
            if(value!=null)
                data.put(name,value);
        }
    }


    /**
     * add a field
     * @param k
     * @param v
     */
    public void put(String k, String v){
        data.put(k,v);
    }

    /**
     * add fields from the Map
     * @param data
     */
    public void putAll(Map<String,String> data){
        this.data.putAll(data);
    }

    /**
     * get all fields as a Map
     * @return
     */
    public Map<String,String> getAll(){
        return new HashMap<String,String>(data);
    }

    /**
     * get a field
     * @param k name of the field
     * @return the field, or null if it does not exist
     */
    public String get(String k){
        return data.get(k);
    }
    public FormSender() {
    }

    /**
     * send the form
     * @param s
     * @param url the http request url (action)
     * @param method "GET" or "POST"
     * @param referer the Referer header
     * @return
     * @throws java.io.IOException
     */
    public Response sendForm(SessionLike s, String url, String method, String referer) throws IOException{
        Response r;
        HashMap<String,String>  dataCopy=null;
        synchronized(data){
            dataCopy=new HashMap<String,String>(data);
        }
        r=s.request(url, method, referer, dataCopy);
        return r;


    }


}