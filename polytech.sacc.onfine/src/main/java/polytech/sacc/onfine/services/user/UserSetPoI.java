package polytech.sacc.onfine.services.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "UserSetPoI", value = "/user/set-poi")
public class UserSetPoI extends HttpServlet {
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User userEntity = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), User.class);
        System.out.println(userEntity);
        //TODO do you bail
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print("Ok");
    }

}


