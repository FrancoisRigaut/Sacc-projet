package polytech.sacc.onfine.services.data;

import com.google.appengine.api.datastore.*;
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
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

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
                    handleCountUsers(resp, admin);
                    break;
                case "count-poi":
                    handleCountPoiUsers(resp, admin);
                    break;
                case "count-position-updates":
                    countPositionUpdates(resp, admin);
                    break;
                case "contacted-poi":
                    countContactedPoi(resp, admin);
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

    private void handleCountUsers(HttpServletResponse resp, Admin loggedAdmin) throws Exception{
        System.out.println("Handle count users");
        sendMail("Ma valeur de test", loggedAdmin);
    }

    private void handleCountPoiUsers(HttpServletResponse resp, Admin loggedAdmin){
        System.out.println("Handle count users poi");
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

        // Delete SQL DBs
        DataSource pool = (DataSource) req.getServletContext().getAttribute(Utils.PG_POOL);
        try (Connection conn = pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE user_poi");
            statement.executeQuery();
            statement.close();
            PreparedStatement statement2 = conn.prepareStatement("TRUNCATE TABLE admin");
            statement2.executeQuery();
            statement2.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("Error while deleting datas in SQL");
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print("All data deleted.");
    }

    private void countPositionUpdates(HttpServletResponse resp, Admin loggedAdmin){
        System.out.println("Handle count users updates");
    }

    private void countContactedPoi(HttpServletResponse resp, Admin loggedAdmin){
        System.out.println("Handle count users contacted poi");
    }

    private void sendMail(String data, Admin adminToSendMail) throws Exception{
        System.out.println(adminToSendMail.toString());
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("damien.montoya26@gmail.com", "The system"));
        msg.addRecipient(Message.RecipientType.TO,
                new InternetAddress(adminToSendMail.getEmail(), "You"));
        msg.setSubject("Your statistics");
        msg.setText(data);
        Transport.send(msg);
    }
}
