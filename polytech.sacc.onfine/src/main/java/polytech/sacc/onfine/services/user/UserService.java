package polytech.sacc.onfine.services.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import org.apache.commons.codec.digest.DigestUtils;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.User;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.utils.NetUtils;
import polytech.sacc.onfine.utils.SqlUtils;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@WebServlet(name = "UserService", value = "/user/*")
public class UserService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        // if you have http://localhost:8080/stats/users/count this will result to stats/users/count
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
            System.out.println("Erreur Post UserService : " + e.getMessage());
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
            System.out.println("Erreur Put UserService : " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleRegisterUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = (JsonObject)NetUtils.getGsonEntity(req, JsonObject.class);
        String phone = jsonObject.get("phone").getAsString();
        System.out.println(phone);
        String sha1 = DigestUtils.sha1Hex(phone);
        System.out.println(sha1);

        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        String kind = "User";
        Key taskKey = datastore.newKeyFactory().setKind(kind).newKey(sha1);

        try {
            Entity retrieved = datastore.get(taskKey);
            System.out.printf("Retrieved %s%n", retrieved.getKey());
            //TODO error
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().print("User already exists");
        } catch (Exception e) {
            Entity task = Entity.newBuilder(taskKey)
                    .build();
            datastore.add(task);
            System.out.printf("Saved %s : %s", task.getKey().getId(), task.getKey().getName());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().printf("User created, sha1 : %s", task.getKey().getName());
        }
    }

    private void handleSetPoiUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        User userEntity = (User)NetUtils.getGsonEntity(req, User.class);
        System.out.println(userEntity);

        ResultSet res = SqlUtils.sqlReqAndRespSet(
                req,
                "SELECT count(*) as numberUser FROM user_poi WHERE sha1 = ?",
                Collections.singletonList(userEntity.getSha1()),
                resp
        );
        if(res != null){
            if(res.getInt("numberUser") > 0){
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                throw new RuntimeException("A user is already PoI for this sha1");
            }
        }

        SqlUtils.sqlReqAndRespBool(
                req,
                "INSERT INTO user_poi VALUES(?)",
                Collections.singletonList(userEntity.getSha1()),
                resp
        );
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().printf("User set to PoI : %s", userEntity.getSha1());
    }
}
