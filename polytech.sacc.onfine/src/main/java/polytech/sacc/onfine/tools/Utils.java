package polytech.sacc.onfine.tools;

import com.google.appengine.repackaged.org.apache.commons.codec.binary.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public abstract class Utils {
    public static final String PROJECT_ID = "sacc-onfine";
    public static final String PROJECT_LOCATION = "europe-west1";
    public static final String PG_POOL = "pg_pool";

    public enum RequestType{
        POST("POST"),
        PUT("PUT");

        private final String type;

        RequestType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static String getCurrentUrl(){
        String hostUrl;
        String environment = System.getProperty("com.google.appengine.runtime.environment");
        if (StringUtils.equals("Production", environment)) {
            hostUrl = "https://sacc-onfine.ew.r.appspot.com";
        } else {
            hostUrl = "http://localhost:8080";
        }
        return hostUrl;
    }

    public static String removeCurrentUrlFromRequestUrl(String requestUrl){
        if(requestUrl.contains("https"))
            return requestUrl.replace(Utils.getCurrentUrl() + "/", "");

        String currentUrl = Utils.getCurrentUrl().replace("https", "http");
        return requestUrl.replace(currentUrl + "/", "");
    }

    public static UtilsResponse makeRequest(String urlString, byte[] bytes, RequestType type) throws Exception{
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod(type.type);
        http.setDoOutput(true);

        int length = bytes.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(bytes);
        }

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return new UtilsResponse(response.toString(), http.getResponseCode());
        }catch (Exception e){
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(http.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return new UtilsResponse(response.toString(), http.getResponseCode());
            }
        }
    }

    //TODO not test donc je sais pas si ca marche
    public static String makeGetRequest(String urlString) throws Exception{
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("GET");
        http.setDoOutput(true);

        http.setFixedLengthStreamingMode(urlString.getBytes().length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}
