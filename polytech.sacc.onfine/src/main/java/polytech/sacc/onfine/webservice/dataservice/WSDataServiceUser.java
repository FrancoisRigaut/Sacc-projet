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
                case "contacted-users":
                    handleLongStatsCalculation(req, resp, parsing[2]);
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
                    handleDeleteAll(req, resp, "/" + requestURL);
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
            resp.getWriter().printf("Task created: %s for url %s. You will receive a mail soon.", task.getName(), requestUrl);
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleLongStatsCalculation(HttpServletRequest req, HttpServletResponse resp, String topicId) throws IOException {
        Publisher publisher = this.publisher;
        try {
            // create a publisher on the topic
            if (publisher == null) {
                ProjectTopicName topicName =
                        ProjectTopicName.newBuilder()
                                .setProject(ServiceOptions.getDefaultProjectId())
                                .setTopic(topicId)
                                .build();
                publisher = Publisher.newBuilder(topicName).build();
            }

            JsonObject jsonObject = new JsonObject();
            for (String param : req.getParameterMap().keySet()) {
                System.out.println(param + " - " + req.getParameter(param));
                jsonObject.addProperty(param, req.getParameter(param));
            }
            String payload = jsonObject.toString();
            PubsubMessage pubsubMessage =
                    PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(payload)).build();

            publisher.publish(pubsubMessage);
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_OK,
                    "Pub sub message launched with payload " + payload + " on topic " + topicId
            );

        } catch (Exception e) {
            NetUtils.sendResponseWithCode(resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e+" - catched in WS"
            );
        }
    }

    private void handleDeleteAll(HttpServletRequest req, HttpServletResponse resp, String requestURL) throws IOException {
        JsonObject jsonObject = (JsonObject) NetUtils.getGsonEntity(req, JsonObject.class);
        try{
            UtilsResponse res = Utils.makeRequest(Utils.getCurrentUrl() + requestURL,
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8),
                    Utils.RequestType.DELETE);
            resp.setStatus(res.getResponseCode());
            resp.getWriter().print(res.getResponse());
        }catch (Exception e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    private Publisher publisher;

    public WSDataServiceUser(){

    }

    public WSDataServiceUser(Publisher publisher) {
        this.publisher = publisher;
    }
}
