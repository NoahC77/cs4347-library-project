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

import static spark.Spark.*;

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
        public int ware_id;
        public int sqft;
        public String state;
        public String city;
        public String street;
        public String ware_name;
    }

    private static void defineEndpoints() {
        listWarehousesEndpoint();
        getWarehouseEndpoint();
        updateWarehouseEndpoint();
        deleteWarehouseEndpoint();
        addWarehouseEndpoint();
    }

    private static void listWarehousesEndpoint(){
        Gson gson = new Gson();
        get("/warehouses", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM warehouse;");

            ArrayList<Warehouse> orders = new ArrayList<>();
            while (resultSet.next()) {
                Warehouse warehouse = new Warehouse(
                        resultSet.getInt("ware_id"),
                        resultSet.getInt("sqft"),
                        resultSet.getString("state"),
                        resultSet.getString("city"),
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
        get("/warehouse/:ware_id", (req, res) -> {
            String s = req.params(":ware_id");
            int ware_id = Integer.parseInt(s);
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM warehouse WHERE ware_id = '"+ware_id+"';");

            ArrayList<Warehouse> orders = new ArrayList<>();
            while (resultSet.next()) {
                Warehouse warehouse = new Warehouse(
                        resultSet.getInt("ware_id"),
                        resultSet.getInt("sqft"),
                        resultSet.getString("state"),
                        resultSet.getString("city"),
                        resultSet.getString("street"),
                        resultSet.getString("ware_name")
                );

                orders.add(warehouse);
            }
            return orders;
        },gson::toJson);
    }
    private static void updateWarehouseEndpoint() {
        Gson gson = new Gson();
        put("/warehouse/:ware_id", (req, res) -> {
            String s = req.params(":ware_id");
            int ware_id = Integer.parseInt(s);
            Warehouse warehouse = gson.fromJson(req.body(), Warehouse.class);
            updateWarehouse(warehouse, ware_id);
            return "Success";
        }, gson::toJson);
    }
    private static void updateWarehouse(Warehouse w, int token) throws SQLException{
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("UPDATE warehouse " +
                "SET ware_name = '"+w.ware_name+"', " +
                "city = '"+w.city+"'," +
                "state = '"+w.state+"'," +
                "street = '"+w.street+"'," +
                "sqft = '"+w.sqft+"'" +
                "WHERE ware_id = '"+token+"'; ");
        }
    private static void deleteWarehouseEndpoint() {
        Gson gson = new Gson();
        delete("/warehouse/:ware_id", (req, res) -> {
            String s = req.params(":ware_id");
            int ware_id = Integer.parseInt(s);
            deleteWarehouse(ware_id);
            return "Success";
        }, gson::toJson);
    }
    private static void deleteWarehouse(int token) throws SQLException{
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("DELETE FROM warehouse WHERE ware_id = '"+token+"'; ");
    }
    private static void addWarehouseEndpoint(){
        Gson gson = new Gson();
        post("/addWarehouse", (req, res) -> {
            Warehouse warehouse = gson.fromJson(req.body(), Warehouse.class);
            addWarehouse(warehouse);
            return "Success";
        },gson::toJson);
    }
    private static void addWarehouse(Warehouse w) throws SQLException{
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("INSERT INTO warehouse (ware_id, ware_name, street, city, state, sqft)" +
                "VALUES ('"+w.ware_id+"','"+w.ware_name+"','"+w.street+"','"+w.city+"','"+w.state+"', '"+w.sqft+"'); ");
    }
}