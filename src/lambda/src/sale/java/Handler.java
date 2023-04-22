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
import java.sql.SQLException;
import java.sql.Statement;
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
        public int itemId;
        public String itemName;
        public int saleId;
        public Date dateSold;
    }

    public static class Sell {
        //These fields are the fields that are present on the item table
        public int itemId;
        public String itemName;
        public int saleId;
        public String dateSold;
    }

    private static void defineEndpoints() {
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
        String check = "we out";
        if(resultSet.next())
        {
            statement.execute("INSERT INTO sales_record (item_id, item_name, sale_id, date_sold) "
                        + "VALUES ('"+s.itemId+"','"+s.itemName+"','"+s.saleId+"','"+d+"');");
            statement.execute("UPDATE item SET current_stock = current_stock-1 " +
                       "WHERE item_id = '"+s.itemId+"';");
            return "success";
        }
        else
        {
            return "No stock available, or invalid item_id / item_name";
        }
    }
}