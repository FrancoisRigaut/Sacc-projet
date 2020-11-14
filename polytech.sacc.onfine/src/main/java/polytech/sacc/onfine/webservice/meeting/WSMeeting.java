package polytech.sacc.onfine.webservice.meeting;

import com.google.appengine.repackaged.com.google.gson.JsonObject;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;
import polytech.sacc.onfine.tools.UtilsResponse;
import polytech.sacc.onfine.utils.NetUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "WSMeetingRegister", value = "/ws/meeting/*")
public class WSMeeting extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/ws/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "register":
                    handleMeetingRegister(req, resp,"/" + requestURL);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/ws/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "find-all":
                    handleFindAll(resp, "/" + requestURL);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleMeetingRegister(HttpServletRequest req, HttpServletResponse resp, String requestUrl) throws IOException{
        JsonObject jsonObject = (JsonObject) NetUtils.getGsonEntity(req, JsonObject.class);
        try{
            if(!jsonObject.has("sha1"))
                throw new MissingArgumentException("sha1");
            if(!jsonObject.has("sha1Met"))
                throw new MissingArgumentException("sha1Met");
            if(!jsonObject.has("gps"))
                throw new MissingArgumentException("gps");
            if(!jsonObject.has("timestamp"))
                throw new MissingArgumentException("timestamp");

            UtilsResponse res = Utils.makeRequest(Utils.getCurrentUrl() + requestUrl,
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8),
                    Utils.RequestType.POST);
            resp.setStatus(res.getResponseCode());
            resp.getWriter().print(res.getResponse());
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleFindAll(HttpServletResponse resp, String requestUrl) throws IOException{
        try{
            UtilsResponse res = Utils.makeGetRequest(Utils.getCurrentUrl() + requestUrl);
            resp.setStatus(res.getResponseCode());
            resp.getWriter().print(res.getResponse());
        }catch (Exception e){
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
}
