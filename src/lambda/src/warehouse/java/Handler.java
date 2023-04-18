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
    public static class Warehouse {
        //These fields are the fields that are present on the item table
        public String wareId;
        public Double sqft;
        public String state;
        public String city;
        public String street;
        public String wareName;
    }

    private static void defineEndpoints() {
        listWarehousesEndpoint();
        getWarehouseEndpoint();
    }

    private static void listWarehousesEndpoint(){
        Gson gson = new Gson();
        get("/warehouses", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM warehouse;");

            ArrayList<Warehouse> orders = new ArrayList<>();
            while (resultSet.next()) {
                Warehouse warehouse = new Warehouse(
                        resultSet.getString("wareId"),
                        resultSet.getDouble("sq_ft"),
                        resultSet.getString("city"),
                        resultSet.getString("state"),
                        resultSet.getString("street"),
                        resultSet.getString("ware_name")
                );

                orders.add(warehouse);
            }
            return orders;
        },gson::toJson);
    }

    private static void getWarehouseEndpoint(){
        Gson gson = new Gson();
        get("/warehouse/:ware_name", (req, res) -> {
            String ware_name = req.params(":ware_name");
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM warehouse WHERE ware_name = '"+ware_name+"';");
            //VALUE ('"+pid+"','"+tid+"','"+rid+"',"+tspent+",'"+des+"')")

            ArrayList<Warehouse> orders = new ArrayList<>();
            while (resultSet.next()) {
                Warehouse warehouse = new Warehouse(
                        resultSet.getString("wareId"),
                        resultSet.getDouble("sq_ft"),
                        resultSet.getString("city"),
                        resultSet.getString("state"),
                        resultSet.getString("street"),
                        resultSet.getString("ware_name")
                );

                orders.add(warehouse);
            }
            return orders;
        },gson::toJson);
    }



}