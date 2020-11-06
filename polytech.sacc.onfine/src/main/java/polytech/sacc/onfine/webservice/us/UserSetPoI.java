package polytech.sacc.onfine.webservice.us;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

@WebServlet(name = "UserSetPoI", value = "/ws/user/setpoi")
public class UserSetPoI extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Properties properties = System.getProperties();

        response.setContentType("text/plain");
        response.getWriter().println("UserSetPoI - Web Service "
                + SystemProperty.version.get() + " Java " + properties.get("java.specification.version"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), JsonObject.class);
        resp.getWriter().print(jsonObject.get("sha1"));

        URL url = new URL("https://localhost:8080/user/set-poi");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        byte[] out =  jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(http.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
    }

    public static String getInfo() {
        return "Version: " + System.getProperty("java.version")
                + " OS: " + System.getProperty("os.name")
                + " User: " + System.getProperty("user.name")
                + " WS User";
    }

}

