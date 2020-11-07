package polytech.sacc.onfine.webservice.dataservice;

import polytech.sacc.onfine.Utils;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "WSDataServiceUser", value = "/ws/stats/users/*")
public class WSDataServiceUser extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/ws/", "");
        // if you have http://localhost:8080/ws/stats/users/count this will result to stats/users/count
        String[] parsing = requestURL.split("/");

        try {
            switch (parsing[2]) {
                case "count":
                case "count-poi":
                    handleFastStatsCalculation(requestURL);
                    break;
                case "count-position-updates":
                case "contacted-poi":
                    handleLongStatsCalculation(requestURL);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
    private void handleFastStatsCalculation(String requestUrl){
        // make cloud tastks for fast calculations
        System.out.println("Fast : " + requestUrl);
    }

    private void handleLongStatsCalculation(String requestUrl){
        // make queue when long calculations
        System.out.println("Long : " + requestUrl);
    }
}
