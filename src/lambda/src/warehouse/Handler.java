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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import static spark.Spark.get;
import static spark.Spark.redirect;

//Might make a super class that contains this boilerplate so we don't need to repeat it.
public class Handler implements RequestStreamHandler {
    // This is generic boilerplate for handlers
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
    //End Boilerplate
    
    //This is a class that defines the structure of the JSON objects that are returned by the /items endpoint
    //The AllArgsConstructor annotation generates a constructor that takes all the fields as parameters
    //This should probably be moved to a separate file but for the sake of documentation I'm keeping it here
    @AllArgsConstructor
    public static class Warehouse {
        //These fields are the fields that are present on the item table
        public String wareID;
        public double squareFeet;
        public String state;
	public String city;
	public street;
	public wareName;
    }

    //This function defines the endpoints for this category
    //End points can be defined in their own functions or be defined in the defineEndpoints function
    private static void defineEndpoints() {
        listItemsEndpoint();
    }
    
    //This function defines the /items endpoint
    private static void listItemsEndpoint(){
        //get is a spark function that defines a get endpoint
        //The first parameter is the endpoint path
        //The second parameter called the handler is a lambda function that is called when the endpoint is called
        //The third parameter is a "response transformer" that converts the return value of the handler function into a string
        //In this case, the lambda function returns an ArrayList of Item objects and the response transformer converts it to JSON Array
        //Note: For the time being authentication isn't implemented so anyone can access this endpoint
        Gson gson = new Gson();
        get("/warehouse", (req, res) -> {
            //This is how you create and execute SQL queries
            //Note: This is not resistant to SQL injection
            //If the endpoint doesn't take any parameters, this is fine
            Statement statement = TestLambdaHandler.conn.createStatement();
            
            //A result set is a table of data returned by a query
            ResultSet resultSet = statement.executeQuery("SELECT * FROM warehouse;");
            //This is just to agregate the results for sending back to the client
            //If the endpoint only returns a single object, you can just return it instead of using a list.
            ArrayList<Item> orders = new ArrayList<>();
            //When iterating over a result set, you need to call next() to move to the next row
            while (resultSet.next()) {
                //Might be worth creating a constructor that pulls the values from a ResultSet
                Warehouse warehouse = new Warehouse(
                    //When getting values from a result set, you need to specify the column name or index
                    resultSet.getString("warehouse_id"),
                    resultSet.getDouble("square_feet"),
                    resultSet.getString("state"),
                    resultSet.getString("city"),
                    resultSet.getString("street"),
                    resultSet.getString("warehouse_name"),
                );
                
                orders.add(Warehouse);
            }
            return orders;
        },gson::toJson);
    }
}
