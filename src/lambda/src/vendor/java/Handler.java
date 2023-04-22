import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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

    private static void defineEndpoints() {
        listVendorsEndpoint();
        getVendorEndpoint();
        searchVendorEndpoint();
        addVendorEndpoint();
    }

    private static void addVendorEndpoint() {
        Gson gson = new Gson();
        put("/addVendor", (req, res) -> {
            Vendor vendor = gson.fromJson(req.body(), Vendor.class);
            String query = "INSERT INTO vendor (vendor_name, state, city, zip_code, street, apt_code) VALUES (?, ?, ?, ?, ?, ?);";
            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query);
            statement.setString(1, vendor.vendorName);
            statement.setString(2, vendor.state);
            statement.setString(3, vendor.city);
            statement.setString(4, vendor.zipCode);
            statement.setString(5, vendor.street);
            statement.setString(6, vendor.aptCode);

            statement.executeUpdate();
            return "Vendor added";
        }, gson::toJson);
    }

    private static void searchVendorEndpoint() {
        Gson gson = new Gson();
        put("/vendorSearch", (req, res) -> {
            SearchRequest searchRequest = gson.fromJson(req.body(), SearchRequest.class);
            String query = "SELECT * FROM vendor WHERE vendor_name LIKE ? OR city LIKE ? OR state LIKE ? OR street LIKE ? OR zip_code LIKE ? OR apt_code LIKE ?;";
            PreparedStatement statement = TestLambdaHandler.conn.prepareStatement(query);
            statement.setString(1, "%" + searchRequest.query + "%");
            statement.setString(2, "%" + searchRequest.query + "%");
            statement.setString(3, "%" + searchRequest.query + "%");
            statement.setString(4, "%" + searchRequest.query + "%");
            statement.setString(5, "%" + searchRequest.query + "%");
            statement.setString(6, "%" + searchRequest.query + "%");

            ResultSet resultSet = statement.executeQuery();

            ArrayList<Vendor> vendors = new ArrayList<>();
            while (resultSet.next()) {
                Vendor vendor = new Vendor(
                        resultSet.getInt("vendor_id"),
                        resultSet.getString("vendor_name"),
                        resultSet.getString("state"),
                        resultSet.getString("city"),
                        resultSet.getString("zip_code"),
                        resultSet.getString("street"),
                        resultSet.getString("apt_code")
                );

                vendors.add(vendor);
            }
            return vendors;
        }, gson::toJson);
    }

    private static void listVendorsEndpoint() {
        Gson gson = new Gson();
        get("/vendors", (req, res) -> {
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor;");

            ArrayList<Vendor> orders = new ArrayList<>();
            while (resultSet.next()) {
                Vendor vendor = new Vendor(
                        resultSet.getInt("vendor_id"),
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
        }, gson::toJson);
    }

    private static void getVendorEndpoint() {
        Gson gson = new Gson();
        get("/vendor/:vendor_name", (req, res) -> {
            String vendor_name = req.params(":vendor_name");
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor WHERE vendor_name = '" + vendor_name + "';");
            //VALUE ('"+pid+"','"+tid+"','"+rid+"',"+tspent+",'"+des+"')")

            ArrayList<Vendor> orders = new ArrayList<>();
            while (resultSet.next()) {
                Vendor vendor = new Vendor(
                        resultSet.getInt("vendor_id"),
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
        }, gson::toJson);
    }



}