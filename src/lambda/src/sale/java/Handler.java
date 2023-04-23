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
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        proxyHandler.proxyStream(input, output, context);
    }

    @AllArgsConstructor
    public static class Sales {
        //These fields are the fields that are present on the item table
        @SerializedName("item_id")
        public int itemId;
        @SerializedName("item_name")
        public String itemName;
        @SerializedName("sale_id")
        public int saleId;
        @SerializedName("date_sold")
        public Date dateSold;
    }

    public static class Sell {
        //These fields are the fields that are present on the item table
        @SerializedName("item_id")
        public int itemId;
    }
    private static void defineEndpoints() {
        SparkUtil.corsRoutes();
        listSalesEndpoint();
        makeSaleEndpoint();
    }

    private static void listSalesEndpoint(){
        Gson gson = new Gson();
        get("/salesHistory", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM sales_record;");

            ArrayList<Sales> orders = new ArrayList<>();
            while (resultSet.next()) {
                Sales sale = new Sales(
                        resultSet.getInt("item_id"),
                        resultSet.getString("item_name"),
                        resultSet.getInt("sale_id"),
                        resultSet.getDate("date_sold")
                );

                orders.add(sale);
            }
            return orders;
        },gson::toJson);
    }

    private static void makeSaleEndpoint(){
        Gson gson = new Gson();
        post("/makeSale", (req, res) -> {
            Sell sale = gson.fromJson(req.body(),Sell.class);
            LocalDateTime date = LocalDateTime.now();
            String formatedS = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(date);
            Date d = Date.valueOf(formatedS);
            return makeSale(sale,d);
        },gson::toJson);
    }

    private static String makeSale(Sell s,Date d) throws SQLException{
        String query = "SELECT item_name FROM item WHERE item_id = ? AND current_stock >= 1;";
        PreparedStatement stmnt = TestLambdaHandler.conn.prepareStatement(query);
        stmnt.setInt(1, s.itemId);
        ResultSet resultSet = stmnt.executeQuery();

        if(resultSet.next())
        {
            String itemName = resultSet.getString("item_name");
            String query1 = "INSERT INTO sales_record (item_id, item_name, date_sold) VALUES (?, ?, ?)";
            PreparedStatement stmnt1 = TestLambdaHandler.conn.prepareStatement(query1);
            stmnt1.setInt(1,s.itemId);
            stmnt1.setString(2,itemName);
            stmnt1.setDate(3, d);
            stmnt1.execute();

            String query2 = "UPDATE item SET current_stock = current_stock-1 WHERE item_id = ?";
            PreparedStatement stmnt2 = TestLambdaHandler.conn.prepareStatement(query2);
            stmnt2.setInt(1,s.itemId);
            stmnt2.execute();

            String query3 = "SELECT MIN(ware_id) as min_ware FROM stored_in;";
            PreparedStatement stmnt3 = TestLambdaHandler.conn.prepareStatement(query3);
            ResultSet resultSet1 = stmnt3.executeQuery();

            resultSet1.next();
            int ware_house = resultSet1.getInt("min_ware");
            String query4 = "UPDATE stored_in SET stock_in_ware = stock_in_ware-1 WHERE item_id = ? " +
            "AND ware_id = ?;";
            PreparedStatement stmnt4 = TestLambdaHandler.conn.prepareStatement(query4);
            stmnt4.setInt(1, s.itemId);
            stmnt4.setInt(2, ware_house);
            stmnt4.execute();
            return "success";
        }
        else
        {
            return "No stock available, or invalid item_id";
        }
    }
}