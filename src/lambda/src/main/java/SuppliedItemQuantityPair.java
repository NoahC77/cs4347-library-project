import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SuppliedItemQuantityPair {
    @SerializedName("supplied_item_id")
    public int suppliedItemId;

    @SerializedName("quantity")
    public int quantity;
}