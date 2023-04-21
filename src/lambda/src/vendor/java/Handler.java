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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        updateVendorEndpoint();
        deleteVendorEndpoint();
        searchVendorEndpoint();
        addVendorEndpoint();
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
        
        addVendorEndpoint();
        searchVendorEndpoint();
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
                        resultSet.getString("vendor_name"),
                        resultSet.getString("state"),
                        resultSet.getString("city"),
                        resultSet.getString("zip_code"),
                        resultSet.getString("street"),
                        resultSet.getString("apt_code"),
                        resultSet.getInt("vendor_id")
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
        get("/vendor/:vendor_id", (req, res) -> {
            String s = req.params(":vendor_id");
            int vendor_id = Integer.parseInt(s);
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM vendor WHERE vendor_id = '"+vendor_id+"';");
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
    private static void updateVendorEndpoint(){
        Gson gson = new Gson();
        put("/vendor/:vendor_id",(req,res) -> {
            String s = req.params(":vendor_id");
            int vendor_id = Integer.parseInt(s);
            Vendor vendor = gson.fromJson(req.body(), Vendor.class);
            updateVendor(vendor,vendor_id);
            return "Success";
        },gson::toJson);
    }
    private static void updateVendor(Vendor vendor, int vendor_id) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("UPDATE vendor " +
                "SET vendor_name = '"+vendor.vendorName+"', " +
                "city = '"+vendor.city+"'," +
                "state = '"+vendor.state+"'," +
                "street = '"+vendor.street+"'," +
                "zip_code = '"+vendor.zipCode+"'," +
                "apt_code = '"+vendor.aptCode+"'," +
                "vendor_id = '"+vendor.vendorId+"'" +
                " WHERE vendor_id = '"+vendor_id+"'; ");
    }
    private static void deleteVendorEndpoint(){
        Gson gson = new Gson();
        delete("/vendor/:vendor_id",(req,res) -> {
            String s = req.params(":vendor_id");
            int vendor_id = Integer.parseInt(s);
            deleteVendor(vendor_id);
            return "Success";
        },gson::toJson);
    }
    private static void deleteVendor(int vendor_id) throws SQLException {
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("DELETE FROM vendor WHERE vendor_id = '"+vendor_id+"';");
    }
    private static void addVendorEndpoint(){
        Gson gson = new Gson();
        post("/addVendor",(req,res) -> {
            Vendor vendor = gson.fromJson(req.body(), Vendor.class);
            addVendor(vendor);
            return "Success";
        },gson::toJson);
    }
    private static void addVendor(Vendor vendor) throws SQLException{
        Statement statement = TestLambdaHandler.conn.createStatement();
        statement.execute("INSERT INTO vendor (vendor_name, city, state, street, zip_code, apt_code, vendor_id)" +
                "VALUES ('"+vendor.vendorName+"','"+vendor.city+"','"+vendor.state+"','"+vendor.street+"'," +
                "'"+vendor.zipCode+"','"+vendor.aptCode+"','"+vendor.vendorId+"');");
    }

}