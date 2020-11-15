package polytech.sacc.onfine.services.data;

import com.google.appengine.repackaged.com.google.gson.JsonObject;
import polytech.sacc.onfine.entity.Message;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.datastore.*;
import polytech.sacc.onfine.entity.User;
import polytech.sacc.onfine.utils.NetUtils;
import polytech.sacc.onfine.utils.SqlUtils;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "DataServiceUser", value = "/stats/users/*")
public class DataUserService extends HttpServlet {

    public DataUserService(){

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = Utils.removeCurrentUrlFromRequestUrl(req.getRequestURL().toString());
        String[] parsing = requestURL.split("/");

        try {
            String email = req.getParameter("admin");
            if(email == null)
                throw new MissingArgumentException("admin");
            Admin admin = new Admin(email);
            if(!isAnAdmin(req, admin)){
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().printf("Admin with sha1 %s does not exists", admin.getEmail());
                return;
            }

            switch (parsing[2]) {
                case "count":
                    handleCountUsers(resp, admin);
                    break;
                case "count-poi":
                    handleCountPoiUsers(req, resp, admin);
                    break;
                case "count-position-updates":
                    handleCountPositionUpdates(resp, admin);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2] + " - for url [" + Utils.getCurrentUrl() + "] - and getRequestUrl was [" + req.getRequestURL() + "]");
            }
        }catch (Exception e){
            System.out.println("Got a problem here : " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String requestURL = Utils.removeCurrentUrlFromRequestUrl(req.getRequestURL().toString());
        String[] parsing = requestURL.split("/");

        try {
            switch (parsing[2]) {
                case "contacted-users":
                    if (verifyToken(req, resp, parsing[2])) {
                        Message message = NetUtils.getMessage(req);
                        JsonObject jsonObject = new Gson().fromJson(message.getData(), JsonObject.class);
                        Admin admin = new Admin(jsonObject.get("admin").getAsString());
                        String sha1 = jsonObject.get("sha1").getAsString();
                        handleContactedUsers(resp, admin, sha1);
                    }
                    break;
            }
        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }

    private boolean verifyToken(HttpServletRequest req, HttpServletResponse resp, String topic) {
        String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
        // Do not process message if request token does not match pubsubVerificationToken
        try {
            if (req.getParameter("token") != null) {
                if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
                    NetUtils.sendErrorMail(topic, "Error: wrong token given in message", new Admin("triagonforce@gmail.com"));
                    NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: wrong token given in message");
                    return false;
                }
            } else {
                NetUtils.sendErrorMail(topic, "Error: missing token in message", new Admin("triagonforce@gmail.com"));
                NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: missing token in message");
                return false;
            }
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isAnAdmin(HttpServletRequest req, Admin adminEntity) throws SQLException{
        DataSource pool = (DataSource) req.getServletContext().getAttribute(Utils.PG_POOL);
        Connection conn = pool.getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT count(*) as numberAdmin FROM admin WHERE email = ?");
        statement.setString(1, adminEntity.getEmail());
        ResultSet res = statement.executeQuery();
        statement.close();
        if(res.next()) {
            return res.getInt("numberAdmin") > 0;
        }
        return false;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

    private void handleCountUsers(HttpServletResponse resp, Admin loggedAdmin) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        int cpt = 0;
        while (results.hasNext()) {
            results.next();
            cpt ++;
        }
        NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, cpt+"");
        try {
            NetUtils.sendResultMail("Number of users", cpt+"", loggedAdmin);
        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error when counting users: " + e.getMessage()
            );
        }
    }

    private void handleCountPositionUpdates(HttpServletResponse resp, Admin loggedAdmin) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        Query<Entity> query1 =
                Query.newEntityQueryBuilder()
                        .setKind("Meeting")
                        .build();
        QueryResults<Entity> results1 = datastore.run(query1);
        int cpt = 0;
        while (results1.hasNext()) {
            results1.next();
            cpt++;
        }

        NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, cpt+"");
        try {
            NetUtils.sendResultMail("Number of position updates", cpt+"", loggedAdmin);
        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error when counting number of position updates: " + e.getMessage()
            );
        }
    }

    private void handleContactedUsers(HttpServletResponse resp, Admin loggedAdmin, String sha1) throws IOException {
        try {
            Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

            Query<Entity> query =
                    Query.newEntityQueryBuilder()
                            .setKind("Meeting")
                            .setFilter(StructuredQuery.PropertyFilter.eq("meeting_sha1", sha1))
                            .build();
            QueryResults<Entity> results = datastore.run(query);
            List<JsonObject> users = new ArrayList<>();
            while (results.hasNext()) {
                Entity entity = results.next();
                JsonObject jsonUser = new JsonObject();
                jsonUser.addProperty("sha1Met", entity.getString("meeting_sha1Met"));
                jsonUser.addProperty("latitude", entity.getDouble("meeting_gps_latitude"));
                jsonUser.addProperty("longitude", entity.getDouble("meeting_gps_longitude"));
                jsonUser.addProperty("timestamp", entity.getString("meeting_timestamp"));
                users.add(jsonUser);
            }

            NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, users.toString());
            NetUtils.sendResultMail("List of users contacted by user: " + sha1, users.toString(), loggedAdmin);
        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error when getting list of contacted user: " + e.getMessage()
            );
        }
    }

    private void handleDeleteAllData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity e = results.next();
            datastore.delete(e.getKey());
        }
        Query<Entity> query2 = Query.newEntityQueryBuilder()
                .setKind("Meeting")
                .build();
        QueryResults<Entity> results2 = datastore.run(query2);
        while (results2.hasNext()) {
            Entity e = results2.next();
            datastore.delete(e.getKey());
        }

        SqlUtils.sqlReqAndRespBool(req, "TRUNCATE TABLE user_poi", new ArrayList<>(), resp);
        SqlUtils.sqlReqAndRespBool(req, "TRUNCATE TABLE admin", new ArrayList<>(), resp);

        NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, "All data deleted.");
    }
}
