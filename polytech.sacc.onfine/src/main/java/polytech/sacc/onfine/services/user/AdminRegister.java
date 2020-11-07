package polytech.sacc.onfine.services.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.entity.Admin;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "AdminRegister", value = "/admin/register")
public class AdminRegister extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Admin adminEntity = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), Admin.class);
        System.out.println(adminEntity);
        //TODO do you bail
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().print("Ok");
    }

}


