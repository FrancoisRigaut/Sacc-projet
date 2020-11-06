package polytech.sacc.onfine;

import com.google.appengine.repackaged.org.apache.commons.codec.binary.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public abstract class Utils {
    public static String getCurrentUrl(){
        String hostUrl;
        String environment = System.getProperty("com.google.appengine.runtime.environment");
        if (StringUtils.equals("Production", environment)) {
            String applicationId = System.getProperty("com.google.appengine.application.id");
            String version = System.getProperty("com.google.appengine.application.version");
            hostUrl = "http://"+version+"."+applicationId+".appspot.com/";
        } else {
            hostUrl = "http://localhost:8080";
        }
        return hostUrl;
    }

    public static String makePostRequest(String urlString, byte[] bytes) throws Exception{
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("GET");
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
            return response.toString();
        }
    }
}
