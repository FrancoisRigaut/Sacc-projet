package polytech.sacc.onfine.repository;

import polytech.sacc.onfine.tools.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SqlUtils {
    public static boolean sqlReqAndRespBool(HttpServletRequest req, String sqlQuery, List<String> params, HttpServletResponse resp) throws IOException {
        try {
            boolean rs = sqlRequestBool(req, sqlQuery, params);
            if (!rs) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("Error while executing request: " + sqlQuery);
            }
            return rs;
        } catch (ServletException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Exception: " + e.getMessage());
            return false;
        }
    }

    public static ResultSet sqlReqAndRespSet(HttpServletRequest req, String sqlQuery, List<String> params, HttpServletResponse resp) throws IOException {
        try {
            ResultSet rs = sqlRequestSet(req, sqlQuery, params);
            if (!rs.next()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("Error while executing request: " + sqlQuery);
            }
            return rs;
        } catch (ServletException | SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Exception: " + e.getMessage());
            return null;
        }
    }

    public static boolean sqlRequestBool(HttpServletRequest req, String sqlQuery, List<String> params) throws ServletException {
        DataSource pool = getPoolFromReq(req);
        try (Connection conn = pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i, params.get(i));
            }
            boolean res = statement.execute();
            statement.close();
            return res;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new ServletException("Unable to successfully connect to the database.", ex);
        }
    }

    public static ResultSet sqlRequestSet(HttpServletRequest req, String sqlQuery, List<String> params) throws ServletException {
        DataSource pool = getPoolFromReq(req);
        try (Connection conn = pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i, params.get(i));
            }
            ResultSet res = statement.executeQuery();
            statement.close();
            return res;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new ServletException("Unable to successfully connect to the database.", ex);
        }
    }

    public static DataSource getPoolFromReq(HttpServletRequest req) {
        return (DataSource) req.getServletContext().getAttribute(Utils.PG_POOL);
    }
}
