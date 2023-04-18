import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class TestLambdaHandler {
    private static final String JDBC_URL = "jdbc:mysql:aws://" + Environment.DB_HOST_NAME + ":" + Environment.DB_PORT + "/CS4347";
    public static final Connection conn;

    static {
        try {
            conn = getDBConnectionUsingIAM();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void prettyPrintEnv(LambdaLogger logger) {
        logger.log("Environment Variables:");
        Arrays.stream(System.getenv().entrySet().toArray())
                .forEach(o -> logger.log((String) o));
    }



    private static Connection getDBConnectionUsingIAM() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, setMySqlConnectionProperties());
    }

    private static Properties setMySqlConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("useAwsIam", "true");
        properties.setProperty("user", Environment.DB_USERNAME);
        return properties;
    }
}
