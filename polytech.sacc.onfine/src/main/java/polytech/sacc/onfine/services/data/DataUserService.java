package polytech.sacc.onfine.services.data;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.utils.NetUtils;
import polytech.sacc.onfine.utils.SqlUtils;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Collectors;

@WebServlet(name = "DataServiceUser", value = "/stats/users/*")
public class DataUserService extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        // if you have http://localhost:8080/stats/users/count this will result to stats/users/count
        String[] parsing = requestURL.split("/");

        try {
            String email = req.getParameter("admin");
            if(email == null)
                throw new MissingArgumentException("admin");
            Admin admin = new Admin(email);

            switch (parsing[2]) {
                case "count":
                    handleCountUsers(req, resp, admin);
                    break;
                case "count-poi":
                    handleCountPoiUsers(req, resp, admin);
                    break;
                case "count-position-updates":
                    countPositionUpdates(req, resp, admin);
                    break;
                case "contacted-poi":
                    countContactedPoi(req, resp, admin);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.println("Got a problem here : " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleCountUsers(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin){

    }

    private void handleCountPoiUsers(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin) throws IOException {
        //Admin entity = (Admin) NetUtils.getGsonEntity(req, Admin.class);
        //List<String> params = Arrays.asList(entity.getEmail());
        try {
            ResultSet rs = SqlUtils.sqlReqAndMailSet(req, "SELECT count(*) AS cpt FROM user_poi", new ArrayList<>(), loggedAdmin);
            if (rs != null) {
                try {
                    NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, rs.getInt("cpt")+"");
                } catch (SQLException e) {
                    NetUtils.sendResponseWithCode(resp,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Error when counting PoI users: " + e.getMessage()
                    );
                }
            } else { // In case of error, mail is already sent in sqlReqAndMail function
                NetUtils.sendResponseWithCode(resp,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error when counting PoI users : Result was null"
                );
            }
        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error when counting PoI users: " + e.getMessage()
            );
        }
    }

    private void countPositionUpdates(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin){
        System.out.println("Handle count users updates");
    }

    private void countContactedPoi(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin){
        System.out.println("Handle count users contacted poi");
    }


}
