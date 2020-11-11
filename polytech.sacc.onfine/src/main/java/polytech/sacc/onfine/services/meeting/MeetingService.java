package polytech.sacc.onfine.services.meeting;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.entity.Meeting;

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
        Meeting meeting = new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), Meeting.class);
        System.out.println(meeting);
        //TODO do you bail
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().print("Ok");
    }
}


