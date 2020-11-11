package polytech.sacc.onfine.services.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import polytech.sacc.onfine.Utils;
import polytech.sacc.onfine.entity.User;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "UserService", value = "/user/*")
public class UserService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        // if you have http://localhost:8080/stats/users/count this will result to stats/users/count
        String[] parsing = requestURL.split("/");

        try {
            switch (parsing[1]) {
                case "register":
                    handleRegisterUser(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.println("Erreur Post UserService : " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        String[] parsing = requestURL.split("/");

        try {
            switch (parsing[1]) {
                case "set-poi":
                    handleSetPoiUser(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.println("Erreur Put UserService : " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleRegisterUser(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        JsonObject jsonObject = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), JsonObject.class);
        String phone = jsonObject.get("phone").getAsString();
        System.out.println(phone);
        //TODO do you bail
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().print("Ok");
    }

    private void handleSetPoiUser(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        User userEntity = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), User.class);
        System.out.println(userEntity);
        //TODO do you bail
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print("Ok");
    }
}
