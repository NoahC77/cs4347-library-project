# Getting started

## Setup
### Requirements
- Java 11 or higher
- AWS CLI Installed and configured

Clone the repository and verify that the project builds:
```bash 
cd src/lambda
./gradlew build
```
### Project structure

```
/src
  /lambda
    /src
      /main
        /java
          /... # Common code
        /resources
          /log4j2.xml # Common resourses
      /item
        /java
          /... # Item endpoints
      /login
        /java
          /... # Login endpoints
      /warehouse
        /java
          /... # Warehouse endpoints
    /... # Other endpoint categories
    /build.gradle.kts # Build configuration
    /settings.gradle.kts # Project configuration
```
All directories under src (excluding main) are an endpoint category. These categories define a set of endpoints that are related to each other. 
For example, the item category contains the following:

```
/items
/item/{id}
/itemSearch
/addItem
```

### Defining endpoints

Each endpoint category has a handler class that defines its endpoints.

#### Handler template
```java
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

    public static class Item {
        public String itemId;
        public int currentStock;
        public String itemName;
        public int sellPrice;
        public int minimumStockLevel;

    }

    private static void defineEndpoints() {
        get("/items", (req, res) -> {
            Gson gson = new Gson();
            Statement statement = TestLambdaHandler.conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM item;");
            ArrayList<Item> orders = new ArrayList<>();
            while (resultSet.next()) {
                Item item = new Item();
                item.itemId = resultSet.getString("item_id");
                item.currentStock = resultSet.getInt("current_stock");
                item.itemName = resultSet.getString("item_name");
                item.sellPrice = resultSet.getInt("sell_price");
                item.minimumStockLevel = resultSet.getInt("minimum_stock_level");
                orders.add(item);
            }
            return gson.toJson(orders);
        });
       
    }
}

```

