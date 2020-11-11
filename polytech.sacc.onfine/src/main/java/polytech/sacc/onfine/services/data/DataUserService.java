package polytech.sacc.onfine.services.data;

import polytech.sacc.onfine.Utils;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DataServiceUser", value = "/stats/users/*")
public class DataUserService extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/", "");
        // if you have http://localhost:8080/stats/users/count this will result to stats/users/count
        String[] parsing = requestURL.split("/");

        try {
            switch (parsing[2]) {
                case "count":
                    handleCountUsers();
                    break;
                case "count-poi":
                    handleCountPoiUsers();
                    break;
                case "count-position-updates":
                    countPositionUpdates();
                    break;
                case "contacted-poi":
                    countContactedPoi();
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
    private void handleCountUsers(){
        System.out.println("Handle count users");
    }

    private void handleCountPoiUsers(){
        System.out.println("Handle count users poi");
    }

    private void countPositionUpdates(){
        System.out.println("Handle count users updates");
    }

    private void countContactedPoi(){
        System.out.println("Handle count users contacted poi");
    }
}
