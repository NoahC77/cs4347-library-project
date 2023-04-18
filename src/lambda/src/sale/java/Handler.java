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
    public static class Sales {
        //These fields are the fields that are present on the item table
        public String itemId;
        public String itemName;
        public String saleName;
        public Date dateSold;
    }

    private static void defineEndpoints() {
        listSalesEndpoint();
    }

    private static void listSalesEndpoint(){
        Gson gson = new Gson();
        get("/salesHistory", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM sales_record;");

            ArrayList<Sales> orders = new ArrayList<>();
            while (resultSet.next()) {
                Sales sale = new Sales(
                        resultSet.getString("item_id"),
                        resultSet.getString("item_name"),
                        resultSet.getString("sale_id"),
                        resultSet.getDate("date_sold")
                );

                orders.add(sale);
            }
            return orders;
        },gson::toJson);
    }
}