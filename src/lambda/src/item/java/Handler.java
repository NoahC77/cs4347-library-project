import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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
    public static class WareItem{
            //These fields are the fields that are present on the item table
            @SerializedName("item_id")
            public int itemId;
            @SerializedName("current_stock")
            public int currentStock;
            @SerializedName("item_name")
            public String itemName;
            @SerializedName("sell_price")
            public int sellPrice;
            @SerializedName("minimum_stock_level")
            public int minimumStockLevel;
            @SerializedName("ware_id")
            public int wareId;
        }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        proxyHandler.proxyStream(input, output, context);
    }

    private static void defineEndpoints() {
        SparkUtil.corsRoutes();
        listItemsEndpoint();
        getItemEndpoint();
        addItemEndpoint();
        updateItemEndpoint();
        deleteItemEndpoint();
        searchItemEndpoint();
    }

    private static void searchItemEndpoint() {
        Gson gson = new Gson();
        put("/itemSearch", (req, res) -> {
            SearchRequest searchRequest = gson.fromJson(req.body(), SearchRequest.class);
            String query = "SELECT * FROM item WHERE item_name LIKE ? ;";
            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query);
            statement.setString(1, "%"+ searchRequest.query+"%");
            ResultSet resultSet = statement.executeQuery();

            ArrayList<Item> items = new ArrayList<>();
            while (resultSet.next()) {
                Item item = new Item(
                        resultSet.getInt("item_id"),
                        resultSet.getInt("current_stock"),
                        resultSet.getString("item_name"),
                        resultSet.getInt("sell_price"),
                        resultSet.getInt("minimum_stock_level")
                );

                items.add(item);
            }
            return items;
        }, gson::toJson);
    }

    private static void listItemsEndpoint() {
        Gson gson = new Gson();
        get("/items", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*" );
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM item;");

            ArrayList<Item> orders = new ArrayList<>();
            while (resultSet.next()) {
                Item item = new Item(
                        resultSet.getInt("item_id"),
                        resultSet.getInt("current_stock"),
                        resultSet.getString("item_name"),
                        resultSet.getInt("sell_price"),
                        resultSet.getInt("minimum_stock_level")
                );

                orders.add(item);
            }
            return orders;
        }, gson::toJson);
    }

    private static void getItemEndpoint(){
        Gson gson = new Gson();
        get("/item/:item_id", (req, res) -> {
            String s = req.params(":item_id");
            int item_id = Integer.parseInt(s);
            Statement statement = TestLambdaHandler.conn.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM item WHERE item_id = '"+item_id+"';");
            ArrayList<Item> orders = new ArrayList<>();
            while (resultSet.next()) {
                Item item = new Item(
                        resultSet.getInt("item_id"),
                        resultSet.getInt("current_stock"),
                        resultSet.getString("item_name"),
                        resultSet.getInt("sell_price"),
                        resultSet.getInt("minimum_stock_level")
                );
                orders.add(item);
            }
            return orders;
        },gson::toJson);
    }

    private static void addItemEndpoint() {
        Gson gson = new Gson();
        post("/addItem", (req, res) -> {
            WareItem item = gson.fromJson(req.body(), WareItem.class);
            addItem(item);
            return "Success";
        },gson::toJson);
    }
    private static void addItem(WareItem item) throws SQLException{
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("INSERT INTO item (item_name, sell_price, minimum_stock_level)" +
                "VALUES ('"+item.itemName+"','"+item.sellPrice+"','"+item.minimumStockLevel+"'); ");

    }

    private static void updateItemEndpoint() {
        Gson gson = new Gson();
        put("/item/:item_id", (req, res) -> {
            String s = req.params(":item_id");
            int item_id = Integer.parseInt(s);
            Item item = gson.fromJson(req.body(), Item.class);
            updateItem(item, item_id);
            return "Success";
        },gson::toJson);
    }
    private static void updateItem(Item item, int item_id)throws SQLException{
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("UPDATE item " +
                "SET item_name = '"+item.itemName+"', " +
                "minimum_stock_level = '"+item.minimumStockLevel+"'," +
                "sell_Price = '"+item.sellPrice+"'" +
                " WHERE item_id = '"+item_id+"'; ");
    }
    private static void deleteItemEndpoint() {
        Gson gson = new Gson();
        delete("/item/:item_id", (req, res) -> {
            String s = req.params(":item_id");
            int item_id = Integer.parseInt(s);
            deleteItem(item_id);
            return "Success";
        },gson::toJson);
    }
    private static void deleteItem(int item_id)throws SQLException{
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("DELETE FROM item WHERE item_id = '"+item_id+"'; ");
    }



}