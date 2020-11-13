package polytech.sacc.onfine.webservice.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;
import polytech.sacc.onfine.tools.UtilsResponse;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@WebServlet(name = "WSUserSetPoI", value = "/ws/user/set-poi")
public class WSUserSetPoI extends HttpServlet {
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), JsonObject.class);
        try{
            if(!jsonObject.has("sha1"))
                throw new MissingArgumentException("sha1");

            UtilsResponse res = Utils.makeRequest(Utils.getCurrentUrl() + "/user/set-poi",
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8),
                    Utils.RequestType.PUT);
            //TODO handle response
            resp.setStatus(res.getResponseCode());
            resp.getWriter().print(res.getResponse());
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
}

