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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
        searchSuppliedItemEndpoint();
    }

    private static void searchSuppliedItemEndpoint() {
        Gson gson = new Gson();
        put("/suppliedItemSearch", (req, res)-> {
            SearchRequest searchRequest = gson.fromJson(req.body(), SearchRequest.class);
            String query = "SELECT * FROM supplied_item " +
                    "LEFT OUTER JOIN item ON supplied_item.item_id = item.item_id " +
                    "LEFT OUTER JOIN vendor ON supplied_item.vendor_id = vendor.vendor_id " +
                    "WHERE item_name LIKE ? OR vendor_name LIKE ? OR supplied_item.item_id LIKE ? OR supplied_item.vendor_id LIKE ? ;";
            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query);
            statement.setString(1, "%" + searchRequest.query + "%");
            statement.setString(2, "%" + searchRequest.query + "%");
            statement.setString(3, "%" + searchRequest.query + "%");
            statement.setString(4, "%" + searchRequest.query + "%");

            ResultSet resultSet = statement.executeQuery();
            ArrayList<HashMap<String, Object>> suppliedItems = new ArrayList<>();
            while (resultSet.next()) {
                SuppliedItem suppliedItem = new SuppliedItem(
                        resultSet.getInt("vendor_id"),
                        resultSet.getInt("item_id"),
                        resultSet.getInt("vendor_price"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("supplied_item_id")
                );
                Item item = new Item(
                        resultSet.getInt("item_id"),
                        resultSet.getInt("current_stock"),
                        resultSet.getString("item_name"),
                        resultSet.getInt("sell_price"),
                        resultSet.getInt("minimum_stock_level")
                );
                Vendor vendor = new Vendor(
                        resultSet.getInt("vendor_id"),
                        resultSet.getString("vendor_name"),
                        resultSet.getString("state"),
                        resultSet.getString("city"),
                        resultSet.getString("zip_code"),
                        resultSet.getString("street"),
                        resultSet.getString("apt_code")
                );

                HashMap<String,Object> result = new HashMap<>();
                result.put("suppliedItem", suppliedItem);
                result.put("item", item);
                result.put("vendor", vendor);
                suppliedItems.add(result);
            }
            return suppliedItems;
        }, gson::toJson);
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
            statement.execute("INSERT INTO supplied_item (item_id, vendor_id, vendor_price, quantity)" +
                    "VALUES ('"+item.itemId+"','"+item.vendorId+"','"+item.vendorPrice+"','"+item.quantity+"'); ");
            //ResultSet set = statement.executeQuery("SELECT AUTO_INCREMENT as a FROM information_schema.TABLES WHERE TABLE_SCHEMA = CS4347 AND TABLE_NAME = supplied_item;");

            ResultSet set = statement.executeQuery("SELECT last_insert_id() as a FROM supplied_item;");
            set.next();
            int id = set.getInt("a");
            statement.execute("INSERT INTO supplies (supplied_item_id, vendor_id) VALUES ('"+id+"', '"+item.vendorId+"');");
            return "success";
        }
        else
        {
            return "Vendor ID or Item ID does not exist!";
        }


    }


}