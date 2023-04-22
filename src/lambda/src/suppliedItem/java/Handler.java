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

    private static void addSuppliedItemEndpoint(){
        Gson gson = new Gson();
        post("/addSuppliedItem", (req, res) -> {
            SuppliedItem item = gson.fromJson(req.body(), SuppliedItem.class);
            addSuppliedItem(item);
            return "Success";
        },gson::toJson);
    }
    private static void addSuppliedItem(SuppliedItem item)throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("INSERT INTO supplied_item (item_id, vendor_id, vendor_price, quantity, supplied_item_id)" +
                "VALUES ('"+item.itemId+"','"+item.vendorId+"','"+item.vendorPrice+"','"+item.quantity+"', '"+item.suppliedItemId+"'); ");
    }


}