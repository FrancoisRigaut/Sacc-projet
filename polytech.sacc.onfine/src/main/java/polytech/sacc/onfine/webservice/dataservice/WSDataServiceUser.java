package polytech.sacc.onfine.webservice.dataservice;

import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.cloud.tasks.v2.*;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.tools.UtilsResponse;
import polytech.sacc.onfine.utils.NetUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import org.apache.http.HttpStatus;


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
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = req.getRequestURL().toString().replace(Utils.getCurrentUrl() + "/ws/", "");
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[2]) {
                case "delete-all":
                    handleDeleteAll(req, resp);
                    break;
                case "random-stat": // TODO TRIAGON
                    handleRandomStat(req, resp, "/stats/users/random-stat");
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
            String queuePath = QueueName.of(Utils.PROJECT_ID, Utils.PROJECT_LOCATION, "sacc-onfine-statistics").toString();
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
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().printf("Task created: %s for url %s. You will receive a meil soon.", task.getName(), requestUrl);
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleLongStatsCalculation(HttpServletResponse resp, String requestUrl){
        // make queue when long calculations
        System.out.println("Long : " + requestUrl);
    }

    private void handleDeleteAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = (JsonObject) NetUtils.getGsonEntity(req, JsonObject.class);

        try{
            UtilsResponse res = Utils.makeRequest(Utils.getCurrentUrl() + "/stats/users/delete-all",
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8),
                    Utils.RequestType.DELETE);

            resp.setStatus(res.getResponseCode());
            resp.getWriter().print(res.getResponse());
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Error when deleting data");
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleRandomStat(HttpServletRequest req, HttpServletResponse resp, String requestUrl) throws IOException {
        Publisher publisher = this.publisher;
        try {
            String topicId = System.getenv("PUBSUB_TOPIC");
            // create a publisher on the topic
            if (publisher == null) {
                ProjectTopicName topicName =
                        ProjectTopicName.newBuilder()
                                .setProject(ServiceOptions.getDefaultProjectId())
                                .setTopic(topicId)
                                .build();
                publisher = Publisher.newBuilder(topicName).build();
            }
            // construct a pubsub message from the payload
            final String payload = req.getParameter("payload");
            PubsubMessage pubsubMessage =
                    PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(payload)).build();

            publisher.publish(pubsubMessage);
            // redirect to home page
            resp.sendRedirect("/");
        } catch (Exception e) {
            resp.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Publisher publisher;

    WSDataServiceUser(Publisher publisher) {
        this.publisher = publisher;
    }
}
