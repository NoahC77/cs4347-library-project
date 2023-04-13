import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
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

    public static class Item {
        public String itemId;
        public int currentStock;
        public String itemName;
        public int sellPrice;
        public int minimumStockLevel;

    }

    private static void defineEndpoints() {
        Gson gson = new Gson();
        get("/items", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM item;");
            ArrayList<Item> orders = new ArrayList<>();
            while (resultSet.next()) {
                Item item = new Item();
                item.itemId = resultSet.getString("item_id");
                item.currentStock = resultSet.getInt("current_stock");
                item.itemName = resultSet.getString("item_name");
                item.sellPrice = resultSet.getInt("sell_price");
                item.minimumStockLevel = resultSet.getInt("minimum_stock_level");
                orders.add(item);
            }
            return orders;
        }, gson::toJson);
    }
}

