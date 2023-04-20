import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import static spark.Spark.get;
import static spark.Spark.redirect;

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

    @AllArgsConstructor
    public static class Employee {
        //These fields are the fields that are present on the item table
        public String username;
        public String password;
        public String first_name;
        public String last_name;
        public String job_title;
        public String state;
        public String city;
        public String zip_code;
        public String street;
        public String apt_num;
    }
    @AllArgsConstructor
    private static class UpdateEmployeeResponse {
        public String response;
        public int errorCode;
    }
    private static void defineEndpoints() {
        listAccountSettingsEndpoint();
        updateAccountSettingsEndpoint();
    }

    private static void listAccountSettingsEndpoint(){
        Gson gson = new Gson();
        get("/accountSettings", (req, res) -> {
            String token = req.headers("Authorization");
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Employee WHERE token = '"+token+"' ;");

            ArrayList<Employee> orders = new ArrayList<>();
            while (resultSet.next()) {
                Employee e = new Employee(
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("job_title"),
                        resultSet.getString("state"),
                        resultSet.getString("city"),
                        resultSet.getString("zip_code"),
                        resultSet.getString("street"),
                        resultSet.getString("apt_num")
                );

                orders.add(e);
            }
            return orders;
        },gson::toJson);
    }
    private static void updateAccountSettingsEndpoint(){
        Gson gson = new Gson();
        get("/accountSettings", (req, res) -> {
            String token = req.headers("Authorization");
            Employee e = gson.fromJson(req.body(), Employee.class);
            int Check = updateAccountSettings(e, token);
            UpdateEmployeeResponse employeeResponse = new UpdateEmployeeResponse("Success", 0);
            return employeeResponse;
        }, gson::toJson);

    }
    private static int updateAccountSettings(Employee e, String t) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
            statement.execute("UPDATE employee " +
                    "SET username = '"+e.username+"', " +
                        "password = '"+e.password+"'" +
                        "first_name = '"+e.first_name+"'" +
                        "last_name = '"+e.last_name+"'" +
                        "state = '"+e.state+"'" +
                        "city = '"+e.city+"'" +
                        "zip_code = '"+e.zip_code+"'" +
                        "street = '"+e.street+"'" +
                        "apt_num = '"+e.apt_num+"'" +
                    " WHERE token = '"+t+"'; ");
        return 0;
    }


}