package polytech.sacc.onfine.webservice.user;

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
import java.util.Arrays;

@WebServlet(name = "WSAdmin", value = "/ws/admin/*")
public class WSAdmin extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/ws/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "register":
                    handleRegisterAdmin(req, resp, "/" + requestURL);
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

    private void handleRegisterAdmin(HttpServletRequest req, HttpServletResponse resp, String requestUrl) throws IOException{
        JsonObject jsonObject = (JsonObject) NetUtils.getGsonEntity(req, JsonObject.class);
        try{
            if(!jsonObject.has("email"))
                throw new MissingArgumentException("email");

            UtilsResponse res = Utils.makeRequest(Utils.getCurrentUrl() + requestUrl,
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8),
                    Utils.RequestType.POST);

            resp.setStatus(res.getResponseCode());
            resp.getWriter().print(res.getResponse());
        }catch (Exception e){
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
}
