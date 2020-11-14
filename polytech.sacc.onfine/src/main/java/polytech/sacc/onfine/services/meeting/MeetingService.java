package polytech.sacc.onfine.services.meeting;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.datastore.*;
import polytech.sacc.onfine.entity.Gps;
import polytech.sacc.onfine.entity.Meeting;
import polytech.sacc.onfine.entity.exception.WrongArgumentException;
import polytech.sacc.onfine.tools.Utils;
import polytech.sacc.onfine.utils.NetUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MeetingService", value = "/meeting/*")
public class MeetingService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = Utils.removeCurrentUrlFromRequestUrl(req.getRequestURL().toString());
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "register":
                    handleRegisterMeeting(req, resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.printf("Erreur Post UserService : %s\n", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURL = Utils.removeCurrentUrlFromRequestUrl(req.getRequestURL().toString());
        String[] parsing = requestURL.split("/");
        try {
            switch (parsing[1]) {
                case "find-all":
                    findAll(resp);
                    break;
                default:
                    throw new WrongArgumentException(parsing[2]);
            }
        }catch (Exception e){
            System.out.printf("Erreur Get UserService : %s\n", e.getMessage());
            resp.getWriter().print(e.getMessage());
        }
    }

    private void handleRegisterMeeting(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        Meeting meeting = (Meeting) NetUtils.getGsonEntity(req, Meeting.class);
        Datastore datastore = connectToDatastore();
        IncompleteKey key = getDatastoreKey(datastore);
        FullEntity<IncompleteKey> newMeeting = FullEntity.newBuilder(key)
                .set("meeting_sha1", meeting.getSha1())
                .set("meeting_sha1Met", meeting.getSha1Met())
                .set("meeting_gps_latitude", meeting.getGps().getLatitude())
                .set("meeting_gps_longitude", meeting.getGps().getLongitude())
                .set("meeting_timestamp", meeting.getTimestamp())
                .build();

        System.out.printf("Meeting registered %s\n", meeting.toString());
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().print(new Gson().toJson(parseEntity(datastore.add(newMeeting))));
    }

    private void findAll(HttpServletResponse resp) throws IOException{
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        List<Meeting> meetings = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Meeting")
                .build();
        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity entity = results.next();
            meetings.add(parseEntity(entity));
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().printf(new Gson().toJson(meetings));
    }

    private Meeting parseEntity(Entity entity){
        Gps tmp = new Gps((float) entity.getDouble("meeting_gps_latitude"), (float) entity.getDouble("meeting_gps_longitude"));
        return new Meeting(
                entity.getString("meeting_sha1"),
                entity.getString("meeting_sha1Met"),
                tmp,
                entity.getString("meeting_timestamp")
        );
    }

    private Datastore connectToDatastore(){
        return DatastoreOptions.getDefaultInstance().getService();
    }

    private IncompleteKey getDatastoreKey(Datastore datastore){
        KeyFactory keyFactory = datastore.newKeyFactory().setKind("Meeting");
        return keyFactory.setKind("Meeting").newKey();
    }
}


