import com.google.gson.annotations.SerializedName;

public class PORequest {
    @SerializedName("supplied_items")
    public SuppliedItemQuantityPair[] suppliedItems;

    @SerializedName("warehouse_id")
    public int warehouseId;
}
