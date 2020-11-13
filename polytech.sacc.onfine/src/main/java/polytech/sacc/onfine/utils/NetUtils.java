package polytech.sacc.onfine.utils;

import com.google.appengine.repackaged.com.google.gson.Gson;
import polytech.sacc.onfine.entity.Admin;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.stream.Collectors;

public class NetUtils {
    public static Object getGsonEntity(HttpServletRequest req, Class c) throws IOException {
        return new Gson().fromJson(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), c);
    }

    public static void sendResultMail(String title, String data, Admin adminToSendMail) throws UnsupportedEncodingException, MessagingException {
        mail("Sacc onfine - Your statistics results",
                "Your result for requesting the following: " + title + " is: \n"+ data,
                adminToSendMail);
    }

    public static void sendErrorMail(String query, String data, Admin adminToSendMail) throws UnsupportedEncodingException, MessagingException {
        mail("Sacc onfine - Error while executing query",
                "Your query: " + query + " raised the following error: \n" + data,
                adminToSendMail);
    }

    public static void mail(String subject, String content, Admin adminToSendMail) throws UnsupportedEncodingException, MessagingException {
        System.out.println("--- Sending mail ---");
        System.out.println(adminToSendMail.toString());
        System.out.println(subject);
        System.out.println(content);
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("damien.montoya26@gmail.com", "The system"));
        msg.addRecipient(Message.RecipientType.TO,
                new InternetAddress(adminToSendMail.getEmail(), "You"));
        msg.setSubject(subject);
        msg.setText(content);
        Transport.send(msg);
    }

    public static void sendResponseWithCode(HttpServletResponse resp, int httpCode, String content) throws IOException {
        resp.setStatus(httpCode);
        resp.getWriter().print(content);
    }
}
