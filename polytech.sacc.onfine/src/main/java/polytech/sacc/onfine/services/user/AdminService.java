package polytech.sacc.onfine.services.user;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.Utils;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.database.DatabaseConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.stream.Collectors;

@WebServlet(name = "AdminRegister", value = "/admin/*")
public class AdminService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "register":
                    handleRegisterAdmin(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.println("Erreur Post AdminService : " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    protected void handleRegisterAdmin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Admin adminEntity = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), Admin.class);
        System.out.println(adminEntity);

        PreparedStatement statement = null;
        try {
            statement = DatabaseConnection.getDatabaseConnection().getConnection().prepareStatement("SELECT count(*) as numberAdmin FROM admin WHERE email = ?");
            statement.setString(1, adminEntity.getEmail());
            ResultSet res = statement.executeQuery();
            statement.close();
            if(res.getInt("numberAdmin") > 0){
                throw new RuntimeException("An admin already exists with this account");
            }

            System.out.println("Number of admins : " + res.getInt("numberAdmin"));

            statement = DatabaseConnection.getDatabaseConnection().getConnection().prepareStatement("INSERT INTO admin(email) VALUES(?)");
            statement.setString(1, adminEntity.getEmail());
            boolean inserted = statement.execute();
            statement.close();

            System.out.println("Inserted : " + inserted);

            if(!inserted)
                throw new RuntimeException("The admin can not be inserted");

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print("Ok");
        }catch (Exception e){
            if(statement != null)
                statement.close();
        }
    }

}


