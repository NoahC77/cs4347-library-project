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
        addPurchaseOrder();
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
            ArrayList<PurchaseOrder> orders = new ArrayList<>();
            while (resultSet.next()) {
                PurchaseOrder vendorPO = new PurchaseOrder(
                        resultSet.getString("po_id"),
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
                        resultSet.getDate("purchase_date")
                );
                orders.add(order);
            }
            System.out.println(gson.toJson(orders));
            return gson.toJson(orders);
        });
    }

    private static class SuppliedItemQuantityPair {
        @SerializedName("supplied_item_id")
        public int suppliedItemId;

        @SerializedName("quantity")
        public int quantity;
    }

    private static void addPurchaseOrder() {
        Gson gson = new Gson();
        post("/addPurchaseOrder", (req, res) -> {
            SuppliedItemQuantityPair[] suppliedItemQuantityPairs = gson.fromJson(req.body(), SuppliedItemQuantityPair[].class);
            String query = "INSERT INTO purchase_order (purchase_date) VALUES (?);";
            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setDate(1, new java.sql.Date(new Date().getTime()));
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            int id = resultSet.getInt(1);

            for (SuppliedItemQuantityPair pair: suppliedItemQuantityPairs) {
                String query2 = "INSERT INTO item_from (po_id, supplied_item_id) VALUES (?, ?);";
                statement = TestLambdaHandler.conn.prepareStatement(query2);
                for (int i = 0; i < pair.quantity; i++) {
                    statement.clearParameters();
                    statement.setInt(1, id);
                    statement.setInt(2, pair.suppliedItemId);
                    statement.executeUpdate();
                }
            }
            ArrayList<Integer> vendorIds = new ArrayList<>();
            for (SuppliedItemQuantityPair pair: suppliedItemQuantityPairs) {
                query = "SELECT vendor_id FROM supplied_item WHERE supplied_item_id = ?;";
                statement = TestLambdaHandler.conn.prepareStatement(query);
                statement.setInt(1, pair.suppliedItemId);
                resultSet = statement.executeQuery();
                resultSet.next();
                int vendorId = resultSet.getInt("vendor_id");
                if (!vendorIds.contains(vendorId)) {
                    vendorIds.add(vendorId);
                }
            }

            query = "INSERT INTO purchased_from (po_id, vendor_id) VALUES (?, ?);";
            statement = TestLambdaHandler.conn.prepareStatement(query);
            for (int vendorId: vendorIds) {
                statement.clearParameters();
                statement.setInt(1, id);
                statement.setInt(2, vendorId);
                statement.executeUpdate();
            }

            return "Success";
        });
    }
}

