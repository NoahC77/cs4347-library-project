import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.utils.Pair;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
        private int total_price;
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
                VendorPO order = new VendorPO(
                        resultSet.getString("vendor_name"),
                        resultSet.getString("po_id"),
                        resultSet.getInt("total_price"),
                        resultSet.getDate("purchase_date")
                );
                orders.add(order);
            }
            return gson.toJson(orders);
        });
    }


    private static void defineGetPurchaseOrders() {
        get("/purchaseOrders", (req, res) -> {
            Gson gson = new Gson();
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor_po;");
            ArrayList<VendorPO> orders = new ArrayList<>();
            while (resultSet.next()) {
                VendorPO order = new VendorPO(
                        resultSet.getString("vendor_name"),
                        resultSet.getString("po_id"),
                        resultSet.getInt("total_price"),
                        resultSet.getDate("purchase_date")
                );
                orders.add(order);
            }
            System.out.println(gson.toJson(orders));
            return gson.toJson(orders);
        });
    }

    private static void addPurchaseOrder() {
        Gson gson = new Gson();
        post("/addPurchaseOrder", (req, res) -> {
            PORequest poRequest = gson.fromJson(req.body(), PORequest.class);

            String query = "INSERT INTO purchase_order (purchase_date) VALUES (?);";
            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setDate(1, new java.sql.Date(new Date().getTime()));
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            int id = resultSet.getInt(1);

            for (SuppliedItemQuantityPair pair : poRequest.suppliedItems) {
                String query2 = "INSERT INTO item_from (po_id, supplied_item_id) VALUES (?, ?);";
                statement = TestLambdaHandler.conn.prepareStatement(query2);
                statement.setInt(1, id);
                statement.setInt(2, pair.suppliedItemId);
                statement.executeUpdate();

            }
            ArrayList<Integer> vendorIds = new ArrayList<>();
            for (SuppliedItemQuantityPair pair : poRequest.suppliedItems) {
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
            for (int vendorId : vendorIds) {
                statement.clearParameters();
                statement.setInt(1, id);
                statement.setInt(2, vendorId);
                statement.executeUpdate();
            }


            final String query2 = "SELECT * FROM stored_in WHERE item_id = ? AND ware_id = ?;";
            final PreparedStatement statement2 = TestLambdaHandler.conn.prepareStatement(query2);
            Arrays.stream(poRequest.suppliedItems).map(suppliedItemQuantityPair ->
                    Pair.of(getItemId(suppliedItemQuantityPair.suppliedItemId), suppliedItemQuantityPair.quantity)
            ).forEach(itemQuantityPair -> {
                try {
                    statement2.clearParameters();
                    statement2.setInt(1, itemQuantityPair.left());
                    statement2.setInt(2, poRequest.warehouseId);
                    ResultSet resultSet2 = statement2.executeQuery();
                    if (resultSet2.next()) {
                        String query3 = "UPDATE stored_in SET stock_in_ware = stock_in_ware + ? WHERE item_id = ? AND ware_id = ?;";
                        PreparedStatement statement3 = TestLambdaHandler.conn.prepareStatement(query3);
                        statement3.setInt(1, itemQuantityPair.right());
                        statement3.setInt(2, itemQuantityPair.left());
                        statement3.setInt(3, poRequest.warehouseId);
                        statement3.executeUpdate();
                    } else {
                        String query3 = "INSERT INTO stored_in (item_id, ware_id, stock_in_ware) VALUES (?, ?, ?);";
                        PreparedStatement statement3 = TestLambdaHandler.conn.prepareStatement(query3);
                        statement3.setInt(1, itemQuantityPair.left());
                        statement3.setInt(2, poRequest.warehouseId);
                        statement3.setInt(3, itemQuantityPair.right());
                        statement3.executeUpdate();
                    }

                    String query4 = "UPDATE item SET current_stock = current_stock + ? WHERE item_id = ?;";
                    PreparedStatement statement4 = TestLambdaHandler.conn.prepareStatement(query4);
                    statement4.setInt(1, itemQuantityPair.right());
                    statement4.setInt(2, itemQuantityPair.left());
                    statement4.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            });


            return "Success";
        });
    }

    public static int getItemId(int suppliedItemId) {
        String query = "SELECT item_id FROM supplied_item WHERE supplied_item_id = ?;";
        PreparedStatement statement = null;
        try {
            statement = TestLambdaHandler.conn.prepareStatement(query);
            statement.setInt(1, suppliedItemId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("item_id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}

