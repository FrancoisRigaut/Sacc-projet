package polytech.sacc.onfine.utils;

import polytech.sacc.onfine.entity.Admin;
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

    public static boolean sqlReqAndMailBool(HttpServletRequest req, String sql, List<String> params, Admin admin) throws Exception {
        try {
            boolean rs = sqlRequestBool(req, sql, params);
            if (!rs) {
                NetUtils.sendErrorMail(sql, "Error while executing request: " + sql, admin);
            }
            return rs;
        } catch (ServletException e) {
            NetUtils.sendErrorMail(sql, "Exception: " + e.getMessage(), admin);
            return false;
        }
    }

    public static ResultSet sqlReqAndMailSet(HttpServletRequest req, String sql, List<String> params, Admin admin) throws Exception {
        try {
            ResultSet rs = sqlRequestSet(req, sql, params);
            if (!rs.next()) {
                NetUtils.sendErrorMail(sql, "Error while executing request: " + sql, admin);
            }
            return rs;
        } catch (ServletException | SQLException e) {
            NetUtils.sendErrorMail(sql, "Exception: " + e.getMessage(), admin);
            return null;
        }
    }

    public static void sqlReqAndRespBool(HttpServletRequest req, String sql, List<String> params, HttpServletResponse resp) throws IOException {
        try {
            boolean rs = sqlRequestBool(req, sql, params);
            // TODO: see why rs is false even if query is executed correctly
//            if (!rs) {
//                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                resp.getWriter().println("Error while executing request: " + sql);
//            }
        } catch (ServletException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Exception: " + e.getMessage());
        }
    }

    /**
     * Execute sql query and return errors in response object (if there is any)
     * @param req HttpServletRequest object
     * @param sql Sql query
     * @param params List of params
     * @param resp HttpServletResponse object
     * @return ResultSet obtained from query
     * @throws IOException if error cannot be written to response object
     */
    public static ResultSet sqlReqAndRespSet(HttpServletRequest req, String sql, List<String> params, HttpServletResponse resp) throws IOException {
        try {
            ResultSet rs = sqlRequestSet(req, sql, params);
            if (!rs.next()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("Error while executing request: " + sql);
            }
            return rs;
        } catch (ServletException | SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Exception: " + e.getMessage());
            return null;
        }
    }

    public static boolean sqlRequestBool(HttpServletRequest req, String sql, List<String> params) throws ServletException {
        DataSource pool = getPoolFromReq(req);
        try (Connection conn = pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i+1, params.get(i));
            }
            boolean res = statement.execute();
            statement.close();
            return res;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new ServletException("Unable to successfully connect to the database.", ex);
        }
    }

    public static ResultSet sqlRequestSet(HttpServletRequest req, String sql, List<String> params) throws ServletException {
        DataSource pool = getPoolFromReq(req);
        try (Connection conn = pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i+1, params.get(i));
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
