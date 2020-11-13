package polytech.sacc.onfine.webservice.dataservice;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.cloud.tasks.v2.*;
import com.google.appengine.api.datastore.Query;
import polytech.sacc.onfine.entity.Admin;
import polytech.sacc.onfine.entity.exception.MissingArgumentException;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.tools.UtilsResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;


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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/ws/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[2]) {
                case "delete-all":
                    handleDeleteAll(req, resp, "/stats/users/delete-all");
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

    private void handleDeleteAll(HttpServletRequest req, HttpServletResponse resp, String requestUrl) throws IOException {
        // Maybe add switchCase
        JsonObject jsonObject = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), JsonObject.class);

        try{
            UtilsResponse res = Utils.makeRequest(Utils.getCurrentUrl() + requestUrl,
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8),
                    Utils.RequestType.POST);

            resp.setStatus(res.getResponseCode());
            resp.getWriter().print(res.getResponse());
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Error 1");
            resp.getWriter().print(e.getMessage());
        }
    }
}
