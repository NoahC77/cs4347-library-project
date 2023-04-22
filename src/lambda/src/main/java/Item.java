import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Item {
    //These fields are the fields that are present on the item table
    @SerializedName("item_id")
    public int itemId;
    @SerializedName("current_stock")
    public int currentStock;
    @SerializedName("item_name")
    public String itemName;
    @SerializedName("sell_price")
    public int sellPrice;
    @SerializedName("minimum_stock_level")
    public int minimumStockLevel;
}
