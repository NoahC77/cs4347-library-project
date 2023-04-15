import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
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
    public static class Item {
        //These fields are the fields that are present on the item table
        @SerializedName("item_id")
        public String itemId;
        @SerializedName("current_stock")
        public int currentStock;
        @SerializedName("item_name")
        public String itemName;
        @SerializedName("sell_price")
        public int sellPrice;
        @SerializedName("minimum_stock_level")
        public int minimumStockLevel;
    }

    private static void defineEndpoints() {
        listItemsEndpoint();
    }

    private static void listItemsEndpoint() {
        Gson gson = new Gson();
        get("/items", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM item;");

            ArrayList<Item> orders = new ArrayList<>();
            while (resultSet.next()) {
                Item item = new Item(
                        resultSet.getString("item_id"),
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
}