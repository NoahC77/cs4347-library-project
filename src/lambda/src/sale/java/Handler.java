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
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.sql.Date;

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
        @SerializedName("item_name")
        public String itemName;
        @SerializedName("sale_id")
        public int saleId;
        @SerializedName("date_sold")
        public String dateSold;
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
            Date date = Date.valueOf(sale.dateSold);
            return makeSale(sale,date);
        },gson::toJson);
    }

    private static String makeSale(Sell s,Date d) throws SQLException{

        Statement statement = TestLambdaHandler.conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM item WHERE item_id = '"+s.itemId+"' AND item_name = '"+s.itemName+"' AND current_stock >= 1;");

        if(resultSet.next())
        {
            statement.execute("INSERT INTO sales_record (item_id, item_name, date_sold) "
                        + "SELECT '"+s.itemId+"', item_name ,'"+s.saleId+"','"+d+"'");
            statement.execute("UPDATE item SET current_stock = current_stock-1 " +
                       "WHERE item_id = '"+s.itemId+"';");
            ResultSet resultSet1 = statement.executeQuery("SELECT MIN(ware_id) as min_ware FROM stored_in;");
            resultSet1.next();
            int ware_house = resultSet1.getInt("min_ware");
            statement.execute("UPDATE stored_in SET stock_in_ware = stock_in_ware-1 WHERE item_id = '"+s.itemId+"' " +
                    "AND ware_id = '"+ware_house+"';");
            return "success";
        }
        else
        {
            return "No stock available, or invalid item_id / item_name";
        }
    }
}