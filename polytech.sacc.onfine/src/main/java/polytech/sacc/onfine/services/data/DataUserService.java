package polytech.sacc.onfine.services.data;

import polytech.sacc.onfine.entity.Message;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonElement;
import com.google.appengine.repackaged.com.google.gson.JsonParser;
import com.google.cloud.datastore.*;
import polytech.sacc.onfine.entity.MessageRepository;
import polytech.sacc.onfine.utils.NetUtils;
import polytech.sacc.onfine.utils.SqlUtils;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

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
import java.util.Base64;
import java.util.stream.Collectors;

@WebServlet(name = "DataServiceUser", value = "/stats/users/*")
public class DataUserService extends HttpServlet {
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
            String sha1;

            switch (parsing[2]) {
                case "count":
                    handleCountUsers(resp, admin);
                    break;
                case "count-poi":
                    handleCountPoiUsers(req, resp, admin);
                    break;
                case "count-position-updates":
                   sha1 = req.getParameter("sha1");
                    if(sha1 == null)
                        throw new MissingArgumentException("sha1");
                    handleCountPositionUpdates(resp, admin, sha1);
                    break;
                case "contacted-poi":
                    sha1 = req.getParameter("sha1");
                    if(sha1 == null)
                        throw new MissingArgumentException("sha1");
                    handleCountContactedPoi(resp, admin, sha1);
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

    private void handleCountPositionUpdates(HttpServletResponse resp, Admin loggedAdmin, String sha1) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        Query<Entity> query1 =
                Query.newEntityQueryBuilder()
                        .setKind("Meeting")
                        .setFilter(StructuredQuery.PropertyFilter.eq("meeting_sha1", sha1))
                        .build();
        QueryResults<Entity> results1 = datastore.run(query1);
        int cpt = 0;
        while (results1.hasNext()) {
            results1.next();
            cpt++;
        }

        NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, cpt+"");
        try {
            NetUtils.sendResultMail("Number of position updates for user " + sha1, cpt+"", loggedAdmin);
        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error when counting number of position updates: " + e.getMessage()
            );
        }
    }

    private void handleCountContactedPoi(HttpServletResponse resp, Admin loggedAdmin, String sha1) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        Query<Entity> query1 =
                Query.newEntityQueryBuilder()
                        .setKind("Meeting")
                        .setFilter(StructuredQuery.PropertyFilter.eq("meeting_sha1Met", sha1))
                        .build();
        QueryResults<Entity> results1 = datastore.run(query1);
        int cpt = 0;
        while (results1.hasNext()) {
            results1.next();
            cpt++;
        }

        NetUtils.sendResponseWithCode(resp, HttpServletResponse.SC_OK, cpt+"");
        try {
            NetUtils.sendResultMail("Number of users contacted by PoI: " + sha1, cpt+"", loggedAdmin);
        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error when counting number of contacted PoI: " + e.getMessage()
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

    private void handleRandomStat(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
        // Do not process message if request token does not match pubsubVerificationToken
        if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // parse message object from "message" field in the request body json
        // decode message data from base64
        Message message = getMessage(req);
        try {
            messageRepository.save(message);
            // 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system
            resp.setStatus(102);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Message getMessage(HttpServletRequest request) throws IOException {
        String requestBody = request.getReader().lines().collect(Collectors.joining("\n"));
        JsonElement jsonRoot = jsonParser.parse(requestBody);
        String messageStr = jsonRoot.getAsJsonObject().get("message").toString();
        Message message = gson.fromJson(messageStr, Message.class);
        // decode from base64
        String decoded = decode(message.getData());
        message.setData(decoded);
        return message;
    }

    private String decode(String data) {
        return new String(Base64.getDecoder().decode(data));
    }

    private final Gson gson = new Gson();
    private final JsonParser jsonParser = new JsonParser();
    private MessageRepository messageRepository;

    public DataUserService(){

    }

    public DataUserService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
}
