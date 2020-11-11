package polytech.sacc.onfine.services.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.Utils;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "AdminRegister", value = "/admin/*")
public class AdminService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        // if you have http://localhost:8080/stats/users/count this will result to stats/users/count
        String[] parsing = requestURL.split("/");

        try {
            switch (parsing[1]) {
                case "register":
                    handleRegisterAdmin(req, resp);
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

    protected void handleRegisterAdmin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Admin adminEntity = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), Admin.class);
        System.out.println(adminEntity);
        //TODO do you bail
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().print("Ok");
    }

}


