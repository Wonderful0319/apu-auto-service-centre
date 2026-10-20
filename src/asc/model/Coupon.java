package asc.model;

/**
 * Coupon issued by Counter Staff to a specific customer.
 * discount is either a fixed RM amount or a percent depending on type.
 */
public class Coupon {
    private String couponId;
    private String customerId;
    private String code;
    private String discountType;  // FIXED / PERCENT
    private double discountValue;
    private String expiryDate;
    private boolean used;
    private String issuedBy;      // staffId who issued it
    private String issuedDate;

    public Coupon(String couponId, String customerId, String code,
                  String discountType, double discountValue,
                  String expiryDate, boolean used,
                  String issuedBy, String issuedDate) {
        this.couponId      = couponId;
        this.customerId    = customerId;
        this.code          = code;
        this.discountType  = discountType;
        this.discountValue = discountValue;
        this.expiryDate    = expiryDate;
        this.used          = used;
        this.issuedBy      = issuedBy;
        this.issuedDate    = issuedDate;
    }

    public String  getCouponId()      { return couponId; }
    public String  getCustomerId()    { return customerId; }
    public String  getCode()          { return code; }
    public String  getDiscountType()  { return discountType; }
    public double  getDiscountValue() { return discountValue; }
    public String  getExpiryDate()    { return expiryDate; }
    public boolean isUsed()           { return used; }
    public void    setUsed(boolean u) { this.used = u; }
    public String  getIssuedBy()      { return issuedBy; }
    public String  getIssuedDate()    { return issuedDate; }

    public String toFileString() {
        return couponId + "|" + customerId + "|" + code + "|"
             + discountType + "|" + String.format("%.2f", discountValue) + "|"
             + expiryDate + "|" + (used ? "1" : "0") + "|"
             + issuedBy + "|" + issuedDate;
    }
}
