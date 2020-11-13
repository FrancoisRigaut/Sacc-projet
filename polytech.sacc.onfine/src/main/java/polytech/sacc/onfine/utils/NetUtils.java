package polytech.sacc.onfine.utils;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.entity.Admin;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

public class NetUtils {
    public static Object getGsonEntity(HttpServletRequest req, Class c) throws IOException {
        return new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), c);
    }

    public static void sendMail(String data, Admin adminToSendMail) throws Exception{
        System.out.println(adminToSendMail.toString());
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("triagonforce@gmail.com", "The system"));
        msg.addRecipient(Message.RecipientType.TO,
                new InternetAddress(adminToSendMail.getEmail(), "You"));
        msg.setSubject("Your statistics");
        msg.setText(data);
        Transport.send(msg);
    }
}
