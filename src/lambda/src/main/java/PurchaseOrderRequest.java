import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderRequest {
    @SerializedName("item_id")
    public String id;
    @SerializedName("quantity")
    public int quantity;
    @SerializedName("price")
    public int price;
    @SerializedName("vendor_id")
    public String vendorId;
    @SerializedName("purchase_date")
    public Date purchaseDate;
}
