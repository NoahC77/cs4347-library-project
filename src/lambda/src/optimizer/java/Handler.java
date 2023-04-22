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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

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
        lowStockEndpoint();
        autoPurchaseOrderEndpoint();
        autoCyclePOEndpoint();
    }

    private static class AutoCyclePORequest {
        @SerializedName("item_id")
        public String itemId;
    }

    private static void autoCyclePOEndpoint() {
        Gson gson = new Gson();
        put("/auto-cycle-po", (req, res) -> {
            AutoCyclePORequest autoCyclePORequest = gson.fromJson(req.body(), AutoCyclePORequest.class);
            String query = "SELECT AVG(sq.sales_count) as average FROM ( " +
                    "SELECT COUNT(*) as sales_count, date_sold " +
                    "FROM sales_record " +
                    "WHERE item_id = ? " +
                    "GROUP BY CEILING(DATEDIFF(date_sold, ?) / ? )" +
                    "HAVING Count(*) >= 1 " +
                    ") sq ;";

            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query);
            statement.setString(1, autoCyclePORequest.itemId);
            statement.setDate(2, Date.valueOf("2020-01-01"));
            statement.setInt(3, 7);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getDouble("average");
        }, gson::toJson);
    }

    private static class AutoPurchaseOrderRequest {
        @SerializedName("item_id")
        public String itemId;
        @SerializedName("quantity")
        public int quantity;
    }

    private static void autoPurchaseOrderEndpoint() {
        Gson gson = new Gson();
        put("/autopurchaseorder", (req, res) -> {
            try {
                AutoPurchaseOrderRequest autoPurchaseOrderRequest = gson.fromJson(req.body(), AutoPurchaseOrderRequest.class);
                List<SuppliedItem> suppliedItems = getSuppliedItems(autoPurchaseOrderRequest.itemId);


                int[] quantities = suppliedItems.stream().mapToInt(suppliedItem -> suppliedItem.quantity).toArray();
                int[] vendorPrices = suppliedItems.stream().mapToInt(suppliedItem -> suppliedItem.vendorPrice).toArray();
                String[] vendorIds = suppliedItems.stream().map(suppliedItem -> suppliedItem.vendorId).toArray(String[]::new);
                int requestedQuantity = autoPurchaseOrderRequest.quantity;

                int minimumCost = findMinimumCost(quantities, vendorPrices, requestedQuantity);
                List<ItemCostQuantity> poItems = findSupliedItemsForCost(quantities, vendorPrices, minimumCost);


                List<Pair<SuppliedItem, Integer>> supplied = poItems.stream().map(itemCostQuantity -> Pair.of(suppliedItems.stream().filter(suppliedItem ->
                                suppliedItem.vendorId.equals(vendorIds[itemCostQuantity.itemIndex]) &&
                                        suppliedItem.quantity == itemCostQuantity.baseQuantity)
                        .findFirst().get(), itemCostQuantity.multiplier)).collect(Collectors.toList());


                return supplied.stream()
                        .map(suppliedItem -> new PurchaseOrderRequest(suppliedItem.left().itemId,
                                suppliedItem.left().quantity,
                                suppliedItem.left().vendorPrice,
                                suppliedItem.left().vendorId,
                                suppliedItem.right()))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new GenericResponse(Collections.emptyList(), false, 500, Collections.singletonMap("content-type", "text/plain"), "Exception thrown.");
        }, gson::toJson);
    }

    public static int findMinimumCost(int[] quantities, int[] costs, int targetQuantity) {
        int n = quantities.length;
        int[][] dp = new int[targetQuantity + 1][n];

        for (int i = 0; i <= targetQuantity; i++) {
            for (int j = 0; j < n; j++) {
                if (i == 0) {
                    dp[i][j] = 0;
                } else if (j == 0) {
                    dp[i][j] = costs[j] + dp[Math.max(0, i - quantities[j])][j];
                } else {
                    dp[i][j] = Math.min(costs[j] + dp[Math.max(0, i - quantities[j])][j], dp[i][j - 1]);
                }
            }
        }

        return dp[targetQuantity][n - 1];
    }

    @AllArgsConstructor
    private static class ItemCostQuantity {
        public int itemIndex;

        public int baseQuantity;
        public int multiplier;
        public int cost;
    }

    public static List<ItemCostQuantity> findSupliedItemsForCost(int[] values, int[] weights, int capacity) {
        int n = values.length;
        int[] dp = new int[capacity + 1];
        int[][] itemQuantities = new int[capacity + 1][n];

        for (int i = 1; i <= capacity; i++) {
            int maxVal = 0;

            for (int j = 0; j < n; j++) {
                if (weights[j] <= i) {
                    int newVal = values[j] + dp[i - weights[j]];

                    if (newVal > maxVal) {
                        maxVal = newVal;
                        System.arraycopy(itemQuantities[i - weights[j]], 0, itemQuantities[i], 0, n);
                        itemQuantities[i][j]++;
                    }
                }
            }

            dp[i] = maxVal;
        }

        List<ItemCostQuantity> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (itemQuantities[capacity][i] > 0) {
                result.add(new ItemCostQuantity(i, values[i], itemQuantities[capacity][i], weights[i]));
            }
        }

        return result;
    }

    private static List<SuppliedItem> getSuppliedItems(String itemId) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM supplied_item WHERE item_id = '" + itemId + "'");
        List<SuppliedItem> suppliedItems = new ArrayList<>();
        while (resultSet.next()) {
            SuppliedItem suppliedItem = new SuppliedItem(
                    resultSet.getString("vendor_id"),
                    resultSet.getString("item_id"),
                    resultSet.getInt("vendor_price"),
                    resultSet.getInt("quantity")
            );
            suppliedItems.add(suppliedItem);
        }
        return suppliedItems;
    }

    @AllArgsConstructor
    private static class SuppliedItem {
        @SerializedName("vendor_id")
        private String vendorId;
        @SerializedName("item_id")
        private String itemId;
        @SerializedName("vendor_price")
        private int vendorPrice;
        @SerializedName("quantity")
        private int quantity;
    }

    private static void lowStockEndpoint() {
        get("/lowstock", (req, res) -> {
            ArrayList<String> items = new ArrayList<>();
            try {
                Statement statement = TestLambdaHandler.conn.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT item_id FROM item WHERE current_stock < minimum_stock_level");
                while (resultSet.next()) {
                    items.add(resultSet.getString("item_id"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Gson().toJson(items);
        });
    }


}