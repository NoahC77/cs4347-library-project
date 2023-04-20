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
    public static class suppliedItem {
        //These fields are the fields that are present on the item table
        @SerializedName("vendor_id")
        public String vendorId;
        @SerializedName("item_id")
        public String itemId;
        @SerializedName("vendor_price")
        public int vendorPrice;
        @SerializedName("quantity")
        public int quantity;

    }

    private static void defineEndpoints() {
        listSuppliedItemsEndpoint();
        getSuppliedItemEndpoint();
    }

    private static void listSuppliedItemsEndpoint() {
        Gson gson = new Gson();
        get("/suppliedItems", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM supplied_item;");

            ArrayList<suppliedItem> orders = new ArrayList<>();
            while (resultSet.next()) {
                suppliedItem item = new suppliedItem(
                        resultSet.getString("vendor_id"),
                        resultSet.getString("item_id"),
                        resultSet.getInt("vendor_price"),
                        resultSet.getInt("quantity")
                );

                orders.add(item);
            }
            return orders;
        }, gson::toJson);
    }

    private static void getSuppliedItemEndpoint(){
        Gson gson = new Gson();
        get("/suppliedItem/:vendor_id:vendor_price", (req, res) -> {
            String vendor_id = req.params(":vendor_id");
            String vendor_price = req.params(":vendor_price");
            Statement statement = TestLambdaHandler.conn.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM supplied_item WHERE vendor_id = '"+vendor_id+"' " +
                    "AND vendor_price = '"+vendor_price+"'  ;");
            ArrayList<suppliedItem> orders = new ArrayList<>();
            while (resultSet.next()) {
                suppliedItem item = new suppliedItem(
                        resultSet.getString("vendor_id"),
                        resultSet.getString("item_id"),
                        resultSet.getInt("vendor_price"),
                        resultSet.getInt("quantity")
                );
                orders.add(item);
            }
            return orders;
        },gson::toJson);
    }



}