import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Vendor {
    //These fields are the fields that are present on the item table
    @SerializedName("vendor_id")
    public int vendorId;
    @SerializedName("vendor_name")
    public String vendorName;
    public String state;
    public String city;
    @SerializedName("zip_code")
    public String zipCode;
    public String street;
    @SerializedName("apt_code")
    public String aptCode;
}
