package scheduleModule;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Response {
    public HttpResponse httpResponse;
    //private String requestUrl;
    //private String text=null;
    private byte[] bytes=null;
    private String url;
    private String DEFAULT_CHARSET="ISO-8859-1";

    public Response(HttpResponse httpResponse, HttpContext context){
        this.httpResponse=httpResponse;

        HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(
                ExecutionContext.HTTP_REQUEST);
        HttpHost currentHost = (HttpHost)  context.getAttribute(
                ExecutionContext.HTTP_TARGET_HOST);
        url = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString() : (currentHost.toURI() + currentReq.getURI());

    }

    public String getUrl(){
        return url;
    }

    public String getText() throws IOException {
        return getText(DEFAULT_CHARSET);
    }

    public String getText(String charset) throws IOException {
        if(bytes==null)getBytes();

        HttpEntity entity=httpResponse.getEntity();
        if(0==entity.getContentLength()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset/*"ISO-8859-1"*//*"UTF8"*/));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch(Exception e){
            throw new IOException();
        }


        return sb.toString();

    }

    public int statusCode(){
        return httpResponse.getStatusLine().getStatusCode();
    }

    public Document parse() throws IOException{
        //return Jsoup.parse(getText(),url);
        return Jsoup.parse(new ByteArrayInputStream(getBytes()), DEFAULT_CHARSET , url);
    }


    public BufferedInputStream getBinaryStream() throws IOException{
        HttpEntity entity=httpResponse.getEntity();

        return new BufferedInputStream(entity.getContent());
    }

    /**
     * Get content as bytes
     * @return
     * @throws java.io.IOException
     */
    public byte[] getBytes() throws IOException{

        HttpEntity entity=httpResponse.getEntity();
        if(0==entity.getContentLength()){
            return new byte[]{};
        }
        InputStream is=new BufferedInputStream(entity.getContent());
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        byte[] buffer=new byte[64*1024];
        int bytesRead;
        while((bytesRead=is.read(buffer,0,buffer.length))!=-1){
            bos.write(buffer,0,bytesRead);
        }
        bos.flush();

        bytes= bos.toByteArray();
        return bytes;
    }


}
