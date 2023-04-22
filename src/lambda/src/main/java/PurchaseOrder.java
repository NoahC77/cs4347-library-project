import lombok.AllArgsConstructor;

import java.util.Date;

@AllArgsConstructor
public class PurchaseOrder {
    public String id;
    public int quantity;
    public int price;
    public Date purchaseDate;

}