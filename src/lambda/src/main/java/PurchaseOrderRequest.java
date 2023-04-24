import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderRequest {
    @SerializedName("item_id")
    public int id;
    @SerializedName("supplied_item_id")
    public int supplied_item_id;
    @SerializedName("quantity")
    public int quantity;
    @SerializedName("price")
    public int price;
    @SerializedName("vendor_id")
    public int vendorId;

    @SerializedName("count")
    public int count;
}
