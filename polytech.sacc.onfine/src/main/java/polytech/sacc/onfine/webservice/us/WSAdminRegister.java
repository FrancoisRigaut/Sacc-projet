package polytech.sacc.onfine.webservice.us;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import polytech.sacc.onfine.Utils;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@WebServlet(name = "WSAdminRegister", value = "/ws/admin/register")
public class WSAdminRegister extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), JsonObject.class);


        try{
            if(!jsonObject.has("email"))
                throw new MissingArgumentException("email");

            String res = Utils.makePostRequest(Utils.getCurrentUrl() + "/admin/register",
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            //TODO handler response
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(res);
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
}
