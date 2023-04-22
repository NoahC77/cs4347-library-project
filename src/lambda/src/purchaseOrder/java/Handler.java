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
import java.sql.PreparedStatement;
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


    private static void defineEndpoints() {
        SparkUtil.corsRoutes();
        defineGetPurchaseOrders();
        searchPurchaseOrders();
    }

    @AllArgsConstructor
    private static class VendorPO {
        private String vendor_name;
        private String po_id;
        private int quantity;
        private int price;
        private Date purchase_date;
    }

    private static void searchPurchaseOrders() {
        put("/purchaseOrderSearch", (req, res) -> {
            Gson gson = new Gson();
            SearchRequest searchRequest = gson.fromJson(req.body(), SearchRequest.class);
            String query = "SELECT * FROM vendor_po WHERE vendor_name LIKE ? OR po_id LIKE ?;";
            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query);
            statement.setString(1, "%" + searchRequest.query + "%");
            statement.setString(2, "%" + searchRequest.query + "%");
            ResultSet resultSet = statement.executeQuery();
            ArrayList<VendorPO> orders = new ArrayList<>();
            while (resultSet.next()) {
                VendorPO vendorPO = new VendorPO(
                        resultSet.getString("vendor_name"),
                        resultSet.getString("po_id"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("price"),
                        resultSet.getDate("purchase_date")
                );
                orders.add(vendorPO);
            }
            return gson.toJson(orders);
        });
    }

    private static void defineGetPurchaseOrders() {
        get("/purchaseOrders", (req, res) -> {
            Gson gson = new Gson();
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM purchase_order;");
            ArrayList<PurchaseOrder> orders = new ArrayList<>();
            while (resultSet.next()) {
                PurchaseOrder order = new PurchaseOrder(
                        resultSet.getString("po_id"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("price"),
                        resultSet.getDate("purchase_date")
                );
                orders.add(order);
            }
            System.out.println(gson.toJson(orders));
            return gson.toJson(orders);
        });
    }
}

