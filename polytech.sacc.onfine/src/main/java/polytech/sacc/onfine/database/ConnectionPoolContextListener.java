package polytech.sacc.onfine.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import polytech.sacc.onfine.tools.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

@WebListener("Creates a connection pool that is stored in the Servlet's context for later use.")
public class ConnectionPoolContextListener implements ServletContextListener {

    private static final String CLOUD_SQL_CONNECTION_NAME = "sacc-onfine:europe-west1:pg-instance-sacc";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "superpassword";
    private static final String DB_NAME = "sacc_onfine";

    private DataSource createConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql:///%s", DB_NAME));
        config.setDriverClassName(org.postgresql.Driver.class.getName());
        config.setUsername(DB_USER); // e.g. "root", "postgres"
        config.setPassword(DB_PASS); // e.g. "my-password"
        config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
        config.addDataSourceProperty("cloudSqlInstance", CLOUD_SQL_CONNECTION_NAME);
        config.setMaximumPoolSize(10000); // maximumPoolSize limits the total number of concurrent connections this pool will keep
        config.setMinimumIdle(10); // minimumIdle is the minimum number of idle connections Hikari maintains in the pool
        config.setConnectionTimeout(10000); // 10 seconds - setConnectionTimeout is the maximum number of milliseconds to wait for a connection checkout
        config.setIdleTimeout(30000); // 5 minutes - idleTimeout is the maximum amount of time a connection can sit in the pool. Connections that sit idle for this many milliseconds are retried if minimumIdle is exceeded.
        config.setMaxLifetime(60000); // 10 minutes - maxLifetime is the maximum possible lifetime of a connection in the pool.
        return new HikariDataSource(config);
    }

    private void createTable(DataSource pool) throws SQLException {
        // Safely attempt to create the table schema.
        try (Connection conn = pool.getConnection()) {
            String stmt =
                    "CREATE TABLE IF NOT EXISTS votes ( "
                            + "vote_id SERIAL NOT NULL, time_cast timestamp NOT NULL, candidate CHAR(6) NOT NULL,"
                            + " PRIMARY KEY (vote_id) );";
            try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
                createTableStatement.execute();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        HikariDataSource pool = (HikariDataSource) event.getServletContext().getAttribute(Utils.PG_POOL);
        if (pool != null) {
            pool.close();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        DataSource pool = (DataSource) servletContext.getAttribute(Utils.PG_POOL);
        if (pool == null) {
            pool = createConnectionPool();
            servletContext.setAttribute(Utils.PG_POOL, pool);
        }
        try {
            createTable(pool);
        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Unable to verify table schema. Please double check the steps"
                            + "in the README and try again.",
                    ex);
        }
    }
}
