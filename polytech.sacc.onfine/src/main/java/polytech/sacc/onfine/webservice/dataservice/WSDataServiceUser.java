package polytech.sacc.onfine.webservice.dataservice;

import com.google.cloud.tasks.v2.*;
import polytech.sacc.onfine.tools.Utils;
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
                    handleFastStatsCalculation(resp, "/" + requestURL + "?" + req.getQueryString());
                    break;
                case "count-position-updates":
                case "contacted-poi":
                    handleLongStatsCalculation(resp, "/" + requestURL + "?" + req.getQueryString());
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }
    private void handleFastStatsCalculation(HttpServletResponse resp, String requestUrl) throws IOException {
        try (CloudTasksClient client = CloudTasksClient.create()) {
            String queuePath = QueueName.of(Utils.PROJECT_ID, Utils.PROJECT_LOCATION, "sacc-onfine-stats").toString();
            // Construct the task body.
            Task.Builder taskBuilder =
                    Task.newBuilder()
                            .setAppEngineHttpRequest(
                                    AppEngineHttpRequest.newBuilder()
                                            .setRelativeUri(requestUrl)
                                            .setHttpMethod(HttpMethod.GET)
                                            .build());
            // Send create task request.
            Task task = client.createTask(queuePath, taskBuilder.build());
            System.out.println("Task created: " + task.getName() + " for url " + requestUrl);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print("Task created: " + task.getName() + " for url " + requestUrl);
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleLongStatsCalculation(HttpServletResponse resp, String requestUrl){
        // make queue when long calculations
        System.out.println("Long : " + requestUrl);
    }
}
