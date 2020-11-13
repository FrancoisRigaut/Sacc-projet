package polytech.sacc.onfine.services.data;

import polytech.sacc.onfine.utils.NetUtils;
import polytech.sacc.onfine.utils.SqlUtils;
import com.google.appengine.api.datastore.*;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[2]) {
                case "delete-all":
                    handleDeleteAllData(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print("All data deleted.");
    }

    private void handleCountUsers(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin) throws Exception{
        System.out.println("Handle count users");
        //NetUtils.sendMail("Ma valeur de test", loggedAdmin);
    }

    private void handleCountPoiUsers(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin) throws IOException {
        //Admin entity = (Admin) NetUtils.getGsonEntity(req, Admin.class);
        //List<String> params = Arrays.asList(entity.getEmail());
        try {
            ResultSet rs = SqlUtils.sqlReqAndMailSet(req, "SELECT count(*) AS cpt FROM user_poi", new ArrayList<>(), loggedAdmin);
            if (rs != null) {
                try {
                    NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, rs.getInt("cpt")+"");
                    NetUtils.sendResultMail("Number of PoI", rs.getInt("cpt")+"", loggedAdmin);
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

    private void countPositionUpdates(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin) {
    }

    private void handleDeleteAllData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Delete NoSQL DBs
        // Use PreparedQuery interface to retrieve results
        PreparedQuery users = datastore.prepare(new Query("User"));
        PreparedQuery meetings = datastore.prepare(new Query("Meeting"));

        for (Entity result : users.asIterable()) {
            resp.getWriter().println(result.toString());
            datastore.delete(result.getKey());
        }
        for (Entity result : meetings.asIterable()) {
            resp.getWriter().println(result.toString());
            datastore.delete(result.getKey());
        }

        SqlUtils.sqlReqAndRespBool(req, "TRUNCATE TABLE user_poi", new ArrayList<>(), resp);
        SqlUtils.sqlReqAndRespBool(req, "TRUNCATE TABLE admin", new ArrayList<>(), resp);

        NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, "All data deleted.");
    }

    private void countPositionUpdates(HttpServletResponse resp, Admin loggedAdmin){
        System.out.println("Handle count users updates");
    }

    private void countContactedPoi(HttpServletRequest req, HttpServletResponse resp, Admin loggedAdmin){
        System.out.println("Handle count users contacted poi");
    }
}
