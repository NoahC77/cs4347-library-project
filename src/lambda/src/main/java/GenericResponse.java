import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
public class GenericResponse {
    public List<String> cookies;
    public boolean isBase64Encoded;
    public int statusCode;
    public Map<String, String> headers;
    public String body;
}

