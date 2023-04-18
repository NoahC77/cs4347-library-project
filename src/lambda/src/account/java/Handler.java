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
    public static class Employee {
        //These fields are the fields that are present on the item table
        public String vendorName;
        public String state;
        public String city;
        public String zipCode;
        public String street;
        public String aptNum;
    }

    private static void defineEndpoints() {
        listVendorsEndpoint();
        getVendorEndpoint();
    }

    private static void listVendorsEndpoint(){
        Gson gson = new Gson();
        get("/vendors", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor;");

            ArrayList<Vendor> orders = new ArrayList<>();
            while (resultSet.next()) {
                Vendor vendor = new Vendor(
                        resultSet.getString("vendor_name"),
                        resultSet.getString("city"),
                        resultSet.getString("state"),
                        resultSet.getString("street"),
                        resultSet.getString("zip_code"),
                        resultSet.getString("apt_code")
                );

                orders.add(vendor);
            }
            return orders;
        },gson::toJson);
    }

    private static void getVendorEndpoint(){
        Gson gson = new Gson();
        get("/vendor/:vendor_name", (req, res) -> {
            String vendor_name = req.params(":vendor_name");
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor WHERE vendor_name = '"+vendor_name+"';");
            //VALUE ('"+pid+"','"+tid+"','"+rid+"',"+tspent+",'"+des+"')")

            ArrayList<Vendor> orders = new ArrayList<>();
            while (resultSet.next()) {
                Vendor vendor = new Vendor(
                        resultSet.getString("vendor_name"),
                        resultSet.getString("city"),
                        resultSet.getString("state"),
                        resultSet.getString("street"),
                        resultSet.getString("zip_code"),
                        resultSet.getString("apt_code")
                );

                orders.add(vendor);
            }
            return orders;
        },gson::toJson);
    }



}