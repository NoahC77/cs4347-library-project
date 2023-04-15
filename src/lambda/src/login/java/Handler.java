import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class Handler implements RequestStreamHandler {
    private static SparkLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> proxyHandler;

    static {
        try {
            proxyHandler = SparkLambdaContainerHandler.getHttpApiV2ProxyHandler();

            defineEndpoints();
            Spark.awaitInitialization();
        } catch (ContainerInitializationException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spark container", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        proxyHandler.proxyStream(input, output, context);
    }

    private static void defineEndpoints() {
        loginEndpoint();
        logoutEndpoint();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    private static class LoginRequest {
        public String username;
        public String password;
    }

    @AllArgsConstructor
    private static class LoginResponse {
        public String token;
        public boolean overwrite;
    }

    private static void setEmployeeToken(String employeeUsername, String token) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("UPDATE employee SET token = '" + token + "' WHERE username = '" + employeeUsername + "';");
    }
    private static int setEmployeeTokenNull(String employeeUsername) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("UPDATE employee SET token = NULL WHERE username = '" + employeeUsername + "';");
        return statement.getUpdateCount();
    }

    private static void loginEndpoint() {
        Gson gson = new Gson();
        post("/login", (req, res) -> {
            LoginRequest login = gson.fromJson(req.body(), LoginRequest.class);
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT token FROM employee WHERE username = '" + login.username + "' AND password = '" + login.password + "';");
            if (resultSet.next()) {
                String currentToken = resultSet.getString("token");
                String newToken = UUID.randomUUID().toString().toLowerCase().replace("-", "");
                setEmployeeToken(login.username, newToken);

                return new LoginResponse(newToken, currentToken != null);
            } else {
                return new GenericResponse(Collections.emptyList(),
                        false,
                        401,
                        Collections.emptyMap(),
                        "Invalid username or password");
            }
        }, gson::toJson);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    private static class LogoutRequest {
        public String username;
    }

    private static void logoutEndpoint() {
        Gson gson = new Gson();
        post("/logout", (req, res) -> {
            LogoutRequest login = gson.fromJson(req.body(), LogoutRequest.class);
            int updateCount = setEmployeeTokenNull(login.username);

            if (updateCount == 1) {
                return new GenericResponse(Collections.emptyList(),
                        false,
                        200,
                        Collections.emptyMap(),
                        "{}");
            } else {
                return new GenericResponse(Collections.emptyList(),
                        false,
                        401,
                        Collections.emptyMap(),
                        "Invalid username");
            }
        }, gson::toJson);
    }
}

