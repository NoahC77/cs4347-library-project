import static spark.Spark.*;

public class SparkUtil {
    public static void corsRoutes() {
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        options("/*", (request, response) -> {
                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                "*");
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }
                    String accessControlRequestOrigin = request
                            .headers("Access-Control-Request-Origin");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Origin",
                                accessControlRequestOrigin);
                    }

                    return "OK";
                });
    }
}
