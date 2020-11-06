<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="polytech.sacc.onfine.webservice.us.UserSetPoI" %>
<html>
<head>
  <link href='//fonts.googleapis.com/css?family=Marmelad' rel='stylesheet' type='text/css'>
  <title>Hello App Engine Standard Java 8</title>
</head>
<body>
    <h1>Hello App Engine -- Java 8 -- Projet Sacc!</h1>

  <p>This is <%= UserSetPoI.getInfo() %>.</p>
  <table>
    <tr>
      <td colspan="2" style="font-weight:bold;">Available Servlets:</td>
    </tr>
    <tr>
      <td><a href='/ws/user/setpoi'>Hello User Set PoI</a></td>
    </tr>
  </table>

</body>
</html>
