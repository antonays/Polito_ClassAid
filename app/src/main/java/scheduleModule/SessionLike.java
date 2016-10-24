package scheduleModule;


import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

public interface SessionLike {

    /**
     * Get session cookies as JSON
     * @return
     */
    public JSONObject dumpCookies();

    /**
     * Load session cookies from JSON
     * @param ck
     */
    public void loadCookies(JSONObject ck);

    /**
     * Make a HTTP Request
     * @param url
     * @param method "GET" or "POST"
     * @param referer 'referer' HTTP header value (can be null)
     * @param data form data (can be null)
     * @return a com.example.anton.classaidtrialslev1.Response object
     * @throws java.io.IOException
     */
    public Response request(String url, String method, String referer, Map<String, String> data) throws IOException;

    /**
     * Same as request("GET", url, referer, null);
     * @param url
     * @param referer
     * @return
     * @throws java.io.IOException
     */
    public Response get(String url, String referer) throws IOException;

    /**
     * Same as request("GET", url, null, null);
     * @param url
     * @return
     * @throws java.io.IOException
     */
    public Response get(String url) throws IOException;

}
