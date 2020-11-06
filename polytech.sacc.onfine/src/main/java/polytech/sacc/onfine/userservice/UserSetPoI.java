package polytech.sacc.onfine.userservice;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

@WebServlet(name = "UserSetPoI", value = "/ws/user/set-poi")
public class UserSetPoI extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Properties properties = System.getProperties();

        response.setContentType("text/plain");
        response.getWriter().println("UserSetPoI - Standard using "
                + SystemProperty.version.get() + " Java " + properties.get("java.specification.version"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), JsonObject.class);
        resp.getWriter().print(jsonObject.get("sha1"));
    }

    public static String getInfo() {
        return "Version: " + System.getProperty("java.version")
                + " OS: " + System.getProperty("os.name")
                + " User: " + System.getProperty("user.name");
    }

}


