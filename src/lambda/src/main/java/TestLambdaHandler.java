import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class TestLambdaHandler implements RequestHandler<Map<String, String>, String> {
    private static final String JDBC_URL = "jdbc:mysql:aws://" + Environment.DB_HOST_NAME + ":" + Environment.DB_PORT+"/CS4347";

    private static void prettyPrintEnv(LambdaLogger logger) {
        logger.log("Environment Variables:");
        Arrays.stream(System.getenv().entrySet().toArray())
                .forEach(o -> logger.log((String) o));
    }

    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(Arrays.toString(System.getenv().entrySet().toArray()));
        try {
            logger.log("CreateConnection");
            Connection conn = getDBConnectionUsingIAM(logger);
            logger.log("CreateStatement");
            Statement statement = conn.createStatement();

            logger.log("ExecuteQuery");
            ResultSet result = statement.executeQuery("SELECT fname,lname FROM employee;");

            logger.log("BuildResponse");
            StringBuilder builder = new StringBuilder();
            while (result.next()) {
                builder.append(result.getTimestamp(1));
                builder.append(", ");
                builder.append(result.getTimestamp(2));
                builder.append("\n");
            }
            logger.log("ReturnResponse");
            return builder.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    private static Connection getDBConnectionUsingIAM(LambdaLogger logger) throws SQLException {
        logger.log("Connection Initialization");
        return DriverManager.getConnection(JDBC_URL, setMySqlConnectionProperties(logger));
    }

    private static Properties setMySqlConnectionProperties(LambdaLogger logger) {
        logger.log("Properties Initialization");
        Properties properties = new Properties();
        properties.setProperty("useAwsIam", "true");
        properties.setProperty("user", Environment.DB_USERNAME);
        logger.log("Properties Finalized");
        return properties;
    }
}
