package polytech.sacc.onfine.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public abstract class DatabaseConnection {
    // Production mode
//    public static DataSource getDatabaseConnection(){
//        // The configuration object specifies behaviors for the connection pool.
//        HikariConfig config = new HikariConfig();
//
//        // Configure which instance and what database user to connect with.
//        config.setJdbcUrl(String.format("jdbc:postgresql:///%s", "sacc_onfine"));
//        config.setUsername("postgres");
//        config.setPassword("superpassword");
//
//        // For Java users, the Cloud SQL JDBC Socket Factory can provide authenticated connections.
//        // See https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory for details.
//        config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
//        config.addDataSourceProperty("cloudSqlInstance", "/cloudsql/sacc-onfine:europe-west1:pg-instance-sacc/.s.PGSQL.543.");
//
//        // Initialize the connection pool using the configuration object.
//        return new HikariDataSource(config);
//    }

    // dev mode
    public static DataSource getDatabaseConnection(){
        Properties connProps = new Properties();
        connProps.setProperty("user", "postgres");
        connProps.setProperty("password", "superpassword");
        connProps.setProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
        connProps.setProperty("cloudSqlInstance", "/cloudsql/sacc-onfine:europe-west1:pg-instance-sacc");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql:///sacc_onfine");
        config.setDataSourceProperties(connProps);
        System.out.println("Init database connection");
        return new HikariDataSource(config);
    }
}
