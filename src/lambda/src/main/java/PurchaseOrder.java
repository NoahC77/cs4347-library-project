import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

import java.util.Date;

@AllArgsConstructor
public class PurchaseOrder {
    @SerializedName("po_id")
    public String id;
    @SerializedName("purchase_date")
    public Date purchaseDate;

}