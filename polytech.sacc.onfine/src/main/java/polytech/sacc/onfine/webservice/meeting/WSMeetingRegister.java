package polytech.sacc.onfine.webservice.meeting;

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

@WebServlet(name = "WSMeetingRegister", value = "/ws/meeting/register")
public class WSMeetingRegister extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), JsonObject.class);
        try{
            if(!jsonObject.has("sha1"))
                throw new MissingArgumentException("sha1");
            if(!jsonObject.has("sha1Met"))
                throw new MissingArgumentException("sha1Met");
            if(!jsonObject.has("gps"))
                throw new MissingArgumentException("gps");
            if(!jsonObject.has("timestamp"))
                throw new MissingArgumentException("timestamp");

            String res = Utils.makeRequest(Utils.getCurrentUrl() + "/meeting/register",
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8),
                    Utils.RequestType.POST);
            //TODO handle response
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(res);
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
}
