import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.rds.endpoints.internal.Value;
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
    public static class SuppliedItem {
        //These fields are the fields that are present on the item table
        @SerializedName("vendor_id")
        public int vendorId;
        @SerializedName("item_id")
        public int itemId;
        @SerializedName("vendor_price")
        public int vendorPrice;
        @SerializedName("quantity")
        public int quantity;
        @SerializedName("supplied_item_id")
        public int suppliedItemId;

    }

    private static void defineEndpoints() {
        listSuppliedItemsEndpoint();
        getSuppliedItemEndpoint();
        updateSuppliedItemEndpoint();
        deleteSuppliedItemEndpoint();
        addSuppliedItemEndpoint();
    }

    private static void listSuppliedItemsEndpoint() {
        Gson gson = new Gson();
        get("/suppliedItems", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM supplied_item;");

            ArrayList<SuppliedItem> orders = new ArrayList<>();
            while (resultSet.next()) {
                SuppliedItem item = new SuppliedItem(
                        resultSet.getInt("vendor_id"),
                        resultSet.getInt("item_id"),
                        resultSet.getInt("vendor_price"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("supplied_item_id")
                );

                orders.add(item);
            }
            return orders;
        }, gson::toJson);
    }

    private static void getSuppliedItemEndpoint(){
        Gson gson = new Gson();
        get("/suppliedItem/:supplied_item_id", (req, res) -> {
            String s = req.params(":supplied_item_id");
            int supplied_item_id = Integer.parseInt(s);
            Statement statement = TestLambdaHandler.conn.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM supplied_item WHERE supplied_item_id = '"+supplied_item_id+"' ;");
            ArrayList<SuppliedItem> orders = new ArrayList<>();
            while (resultSet.next()) {
                SuppliedItem item = new SuppliedItem(
                        resultSet.getInt("vendor_id"),
                        resultSet.getInt("item_id"),
                        resultSet.getInt("vendor_price"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("supplied_item_id")
                );
                orders.add(item);
            }
            return orders;
        },gson::toJson);
    }

    private static void updateSuppliedItemEndpoint(){
        Gson gson = new Gson();
        put("/suppliedItem/:supplied_item_id",(req,res) -> {
            String s = req.params(":supplied_item_id");
            int id = Integer.parseInt(s);
            SuppliedItem item = gson.fromJson(req.body(), SuppliedItem.class);
            return updateSuppliedItem(item,id);
        },gson::toJson);
    }
    private static String updateSuppliedItem(SuppliedItem item , int id) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor, item WHERE vendor_id = '"+item.vendorId+"' AND " +
                "item_id = '"+item.itemId+"';");
        if(resultSet.next()){
            statement.execute("UPDATE supplied_item " +
                    "SET vendor_id = '"+item.vendorId+"', " +
                    "item_id = '"+item.itemId+"'," +
                    "vendor_price = '"+item.vendorPrice+"'," +
                    "quantity = '"+item.quantity+"'," +
                    "supplied_item_id = '"+id+"'" +
                    " WHERE supplied_item_id = '"+id+"'; ");
            return "Success";
        }
        else{
            return "Vendor ID or Item ID Does not Exist!";
        }

    }

    private static void deleteSuppliedItemEndpoint(){
        Gson gson = new Gson();
        delete("/suppliedItem/:supplied_item_id",(req,res) -> {
            String s = req.params(":supplied_item_id");
            int id = Integer.parseInt(s);
            SuppliedItem item = gson.fromJson(req.body(), SuppliedItem.class);
            deleteSuppliedItem(id);
            return "Success";
        },gson::toJson);
    }
    private static void deleteSuppliedItem(int id) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("DELETE FROM supplied_item WHERE supplied_item_id = '"+id+"'; ");
        // SQL cascades delete on 'supplies' table. Dont worry about it.
    }

    private static void addSuppliedItemEndpoint(){
        Gson gson = new Gson();
        post("/addSuppliedItem", (req, res) -> {
            SuppliedItem item = gson.fromJson(req.body(), SuppliedItem.class);
            return addSuppliedItem(item);
        },gson::toJson);
    }
    private static String addSuppliedItem(SuppliedItem item)throws SQLException {
    // Adding a suppliedItem checks for the existence of the item and vendor and also adds it to 'supplies'
        Statement statement = TestLambdaHandler.conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor, item WHERE vendor_id = '"+item.vendorId+"' AND " +
                "item_id = '"+item.itemId+"';");

        if(resultSet.next())
        {
            statement.execute("INSERT INTO supplied_item (item_id, vendor_id, vendor_price, quantity, supplied_item_id)" +
                    "VALUES ('"+item.itemId+"','"+item.vendorId+"','"+item.vendorPrice+"','"+item.quantity+"', '"+item.suppliedItemId+"'); ");
            statement.execute("INSERT INTO supplies (supplied_item_id, vendor_id) VALUES ('"+item.suppliedItemId+"', '"+item.vendorId+"');");
            return "success";
        }
        else
        {
            return "Vendor ID or Item ID does not exist!";
        }


    }


}