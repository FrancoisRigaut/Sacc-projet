package polytech.sacc.onfine.services.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.User;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.utils.NetUtils;
import polytech.sacc.onfine.utils.SqlUtils;

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
import java.util.Collections;
import java.util.List;

@WebServlet(name = "UserService", value = "/user/*")
public class UserService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = Utils.removeCurrentUrlFromRequestUrl(req.getRequestURL().toString());
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "register":
                    handleRegisterUser(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.printf("Erreur Post UserService : %s\n", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "set-poi":
                    handleSetPoiUser(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.printf("Erreur Put UserService : %s\n", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = Utils.removeCurrentUrlFromRequestUrl(req.getRequestURL().toString());
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "find-all":
                    findAll(resp);
                    break;
                case "find-all-poi":
                    findAllPoi(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.printf("Erreur Get UserService : %s\n", e.getMessage());
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleRegisterUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = (JsonObject)NetUtils.getGsonEntity(req, JsonObject.class);
        String phone = jsonObject.get("phone").getAsString();
        String sha1 = DigestUtils.sha1Hex(phone);
        System.out.printf("User with phone %s has sha1 %s\n", phone, sha1);

        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        Key taskKey = datastore.newKeyFactory().setKind("User").newKey(sha1);
        try {
            Entity retrieved = datastore.get(taskKey);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            System.out.printf("User with sha1 %s already exists\n", retrieved.getKey().getName());
            resp.getWriter().printf("User with sha1 %s already exists", retrieved.getKey().getName());
            return;
        } catch (Exception ignore) { }

        Entity task = Entity.newBuilder(taskKey)
                .build();
        datastore.add(task);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        System.out.printf("User registered, sha1 : %s\n", task.getKey().getName());
        resp.getWriter().printf("User registered, sha1 : %s", task.getKey().getName());
    }

    public static boolean isUserExisting(User user){
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        Key taskKey = datastore.newKeyFactory().setKind("User").newKey(user.getSha1());
        try {
            Entity entity = datastore.get(taskKey);
            return entity != null;
        }catch (Exception e) {
            System.out.println("TEST");
            return false;
        }
    }

    private void handleSetPoiUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        User userEntity = (User)NetUtils.getGsonEntity(req, User.class);
        if(!isUserExisting(userEntity)){
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            System.out.printf("User with sha1 %s does not exists\n", userEntity.getSha1());
            resp.getWriter().printf("User with sha1 %s does not exists", userEntity.getSha1());
            return;
        }

        ResultSet res = SqlUtils.sqlReqAndRespSet(
                req,
                "SELECT count(*) as numberUser FROM user_poi WHERE sha1 = ?",
                Collections.singletonList(userEntity.getSha1()),
                resp
        );
        if(res != null){
            if(res.getInt("numberUser") > 0){
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                System.out.printf("User with sha1 %s is already Poi\n", userEntity.getSha1());
                resp.getWriter().printf("User with sha1 %s is already Poi", userEntity.getSha1());
                return;
            }
        }
        SqlUtils.sqlReqAndRespBool(
                req,
                "INSERT INTO user_poi VALUES(?)",
                Collections.singletonList(userEntity.getSha1()),
                resp
        );
        resp.setStatus(HttpServletResponse.SC_CREATED);
        System.out.printf("User set to PoI : %s\n", userEntity.getSha1());
        resp.getWriter().printf("User set to PoI : %s", userEntity.getSha1());
    }

    private void findAll(HttpServletResponse resp) throws IOException{
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        List<User> users = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity entity = results.next();
            users.add(new User(entity.getKey().getName()));
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().printf(new Gson().toJson(users));
    }

    private void findAllPoi(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException{
        DataSource pool = (DataSource) req.getServletContext().getAttribute(Utils.PG_POOL);

        Connection conn = pool.getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM user_poi");
        ResultSet res = statement.executeQuery();
        List<User> users = new ArrayList<>();
        while(res.next()){
            users.add(new User(res.getString("sha1")));
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().printf(new Gson().toJson(users));
    }
}
