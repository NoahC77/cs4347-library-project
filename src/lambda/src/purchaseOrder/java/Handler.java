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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import static spark.Spark.get;

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

    public static class PurchaseOrder {
        public String id;
        public int quantity;
        public int price;
        public Date purchaseDate;

    }

    private static void defineEndpoints() {
        defineGetPurchaseOrders();
    }

    private static void defineGetPurchaseOrders() {
        get("/purchaseOrders", (req, res) -> {
            Gson gson = new Gson();
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM purchase_order;");
            ArrayList<PurchaseOrder> orders = new ArrayList<>();
            while (resultSet.next()) {
                PurchaseOrder order = new PurchaseOrder();
                order.id = resultSet.getString("po_id");
                order.quantity = resultSet.getInt("quantity");
                order.price = resultSet.getInt("price");
                order.purchaseDate = resultSet.getDate("purchase_date");
                orders.add(order);

            }
            System.out.println(gson.toJson(orders));
            return gson.toJson(orders);
        });
    }
}

