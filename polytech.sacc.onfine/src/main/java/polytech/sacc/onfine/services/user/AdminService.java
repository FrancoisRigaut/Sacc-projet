package polytech.sacc.onfine.services.user;

import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.utils.NetUtils;

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

@WebServlet(name = "AdminRegister", value = "/admin/*")
public class AdminService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = Utils.removeCurrentUrlFromRequestUrl(req.getRequestURL().toString());
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
            resp.getWriter().print(e.getMessage());
        }
    }

    protected void handleRegisterAdmin(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        Admin adminEntity = (Admin) NetUtils.getGsonEntity(req, Admin.class);
        DataSource pool = (DataSource) req.getServletContext().getAttribute(Utils.PG_POOL);

        Connection conn = pool.getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT count(*) as numberAdmin FROM admin WHERE email = ?");
        statement.setString(1, adminEntity.getEmail());
        ResultSet res = statement.executeQuery();
        statement.close();
        if(res.next()){
            if(res.getInt("numberAdmin") > 0){
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().printf("Admin with sha1 %s already exists", adminEntity.getEmail());
                return;
            }
        }else{
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException("Error while fetching admins");
        }
        statement = conn.prepareStatement("INSERT INTO admin(email) VALUES(?)");
        statement.setString(1, adminEntity.getEmail());
        statement.execute();
        statement.close();
        System.out.printf("Admin registered %s", adminEntity.getEmail());
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().printf("Admin registered %s", adminEntity.getEmail());
    }
}


