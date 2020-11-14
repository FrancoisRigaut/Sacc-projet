package polytech.sacc.onfine.services.meeting;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.datastore.*;
import polytech.sacc.onfine.entity.Gps;
import polytech.sacc.onfine.entity.Meeting;
import polytech.sacc.onfine.utils.NetUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "MeetingService", value = "/meeting/register")
public class MeetingService extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Meeting meeting = (Meeting) NetUtils.getGsonEntity(req, Meeting.class);
        System.out.println(meeting);

        Datastore datastore = connectToDatastore();
        IncompleteKey key = getDatastoreKey(datastore);

        FullEntity<IncompleteKey> newMeeting = FullEntity.newBuilder(key)
                .set("meeting_sha1", meeting.getSha1())
                .set("meeting_sha1Met", meeting.getSha1Met())
                .set("meeting_gps_latitude", meeting.getGps().getLatitude())
                .set("meeting_gps_longitude", meeting.getGps().getLongitude())
                .set("meeting_timestamp", meeting.getTimestamp())
                .build();

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().print(new Gson().toJson(parseEntity(datastore.add(newMeeting))));
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


