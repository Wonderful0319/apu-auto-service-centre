package asc.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Redesigned VIP system – 4 tiers:
 *   NONE  (no tier)  →  0 pts
 *   BRONZE           →  1 000 pts
 *   GOLD             →  10 000 pts
 *   BLACKGOLD        →  100 000 pts
 *
 * Points are earned 1 pt per RM spent (service payments + self top-ups).
 *
 * Benefits:
 *   BRONZE    – RM 20 coupon issued every calendar month
 *   GOLD      – BRONZE coupons + 2 % discount on all services
 *   BLACKGOLD – RM 50 coupon issued every calendar month + 5 % discount
 */
public class VipAccount {

    // ── Tier identifiers ─────────────────────────────────────────────────────
    public static final String TIER_NONE      = "NONE";
    public static final String TIER_BRONZE    = "BRONZE";
    public static final String TIER_GOLD      = "GOLD";
    public static final String TIER_BLACKGOLD = "BLACKGOLD";

    // ── Thresholds (cumulative points) ───────────────────────────────────────
    public static final int THRESHOLD_BRONZE    =   1_000;
    public static final int THRESHOLD_GOLD      =  10_000;
    public static final int THRESHOLD_BLACKGOLD = 100_000;

    // ── Service discounts (%) ────────────────────────────────────────────────
    public static final double DISCOUNT_NONE      = 0.0;
    public static final double DISCOUNT_BRONZE    = 0.0;   // no % discount
    public static final double DISCOUNT_GOLD      = 2.0;
    public static final double DISCOUNT_BLACKGOLD = 5.0;

    // ── Monthly coupon values ─────────────────────────────────────────────────
    public static final double COUPON_BRONZE    = 20.0;
    public static final double COUPON_BLACKGOLD = 50.0;

    // ── Legacy / unused constants kept for file-format compatibility ─────────
    public static final String SUB_NONE       = "NONE";
    public static final String SUB_BLACK_CARD = "BLACK_CARD";
    public static final double DISCOUNT_STANDARD = 0.0;

    // ── Fields ───────────────────────────────────────────────────────────────
    private String vipId;
    private String customerId;
    private String phone;
    private int    points;          // cumulative lifetime points
    private String tier;
    private String createdDate;
    private double totalSpent;
    private double discountPct;     // current tier discount %
    private String lastCouponMonth; // "yyyy-MM" of last monthly coupon issued
    private String subscriptionType;   // kept for legacy file compat
    private String subscriptionExpiry; // kept for legacy file compat

    // ── Canonical constructor (12 fields matching toFileString) ──────────────
    public VipAccount(String vipId, String customerId, String phone,
                      int points, String tier, String createdDate,
                      double totalSpent, double discountPct,
                      int unused1, int unused2,          // legacy valetUsed / valetQuota
                      String subscriptionType, String subscriptionExpiry) {
        this.vipId              = vipId;
        this.customerId         = customerId;
        this.phone              = phone;
        this.points             = points;
        this.createdDate        = createdDate;
        this.totalSpent         = totalSpent;
        this.subscriptionType   = subscriptionType;
        this.subscriptionExpiry = subscriptionExpiry;
        // lastCouponMonth is stored in subscriptionExpiry field for new records;
        // detect by format "yyyy-MM"
        if (subscriptionExpiry != null && subscriptionExpiry.matches("\\d{4}-\\d{2}")) {
            this.lastCouponMonth = subscriptionExpiry;
            this.subscriptionExpiry = "N/A";
        } else {
            this.lastCouponMonth = "";
        }
        recalcTier();
    }

    /** Legacy 6-field constructor (from old files). */
    public VipAccount(String vipId, String customerId, String phone,
                      double balance, String oldTier, String createdDate) {
        this.vipId              = vipId;
        this.customerId         = customerId;
        this.phone              = phone;
        this.points             = (int) balance;
        this.createdDate        = createdDate;
        this.totalSpent         = balance;
        this.subscriptionType   = SUB_NONE;
        this.subscriptionExpiry = "N/A";
        this.lastCouponMonth    = "";
        recalcTier();
    }

    // ── Enrolment factory ─────────────────────────────────────────────────────
    public static VipAccount enrol(String vipId, String customerId, String phone) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        VipAccount v = new VipAccount(vipId, customerId, phone,
                0, TIER_NONE, today, 0.0, 0.0, 0, 0, SUB_NONE, "N/A");
        v.lastCouponMonth = "";
        return v;
    }

    // ── Points & tier ─────────────────────────────────────────────────────────
    /** Add points equal to RM amount spent. Recalculates tier. */
    public void addPointsFromPayment(double amountRM) {
        this.points     += (int) amountRM;
        this.totalSpent += amountRM;
        recalcTier();
    }

    public void recalcTier() {
        if      (points >= THRESHOLD_BLACKGOLD) { tier = TIER_BLACKGOLD; discountPct = DISCOUNT_BLACKGOLD; }
        else if (points >= THRESHOLD_GOLD)      { tier = TIER_GOLD;      discountPct = DISCOUNT_GOLD;      }
        else if (points >= THRESHOLD_BRONZE)    { tier = TIER_BRONZE;    discountPct = DISCOUNT_BRONZE;    }
        else                                    { tier = TIER_NONE;      discountPct = DISCOUNT_NONE;      }
    }

    // ── Tier progression ──────────────────────────────────────────────────────
    public int pointsToNextTier() {
        if (points >= THRESHOLD_BLACKGOLD) return 0;
        if (points >= THRESHOLD_GOLD)      return THRESHOLD_BLACKGOLD - points;
        if (points >= THRESHOLD_BRONZE)    return THRESHOLD_GOLD    - points;
        return THRESHOLD_BRONZE - points;
    }

    public int currentTierFloor() {
        if (points >= THRESHOLD_BLACKGOLD) return THRESHOLD_BLACKGOLD;
        if (points >= THRESHOLD_GOLD)      return THRESHOLD_GOLD;
        if (points >= THRESHOLD_BRONZE)    return THRESHOLD_BRONZE;
        return 0;
    }

    public int nextTierThreshold() {
        if (points >= THRESHOLD_BLACKGOLD) return THRESHOLD_BLACKGOLD;
        if (points >= THRESHOLD_GOLD)      return THRESHOLD_BLACKGOLD;
        if (points >= THRESHOLD_BRONZE)    return THRESHOLD_GOLD;
        return THRESHOLD_BRONZE;
    }

    public String nextTierName() {
        if (points >= THRESHOLD_BLACKGOLD) return "MAX TIER";
        if (points >= THRESHOLD_GOLD)      return "BLACKGOLD";
        if (points >= THRESHOLD_BRONZE)    return "GOLD";
        return "BRONZE";
    }

    /** 0.0 – 1.0 progress within the current tier band. */
    public double tierProgressRatio() {
        if (points >= THRESHOLD_BLACKGOLD) return 1.0;
        int floor = currentTierFloor();
        int ceil  = nextTierThreshold();
        if (ceil == floor) return 1.0;
        return (double)(points - floor) / (ceil - floor);
    }

    // ── Monthly coupon logic ──────────────────────────────────────────────────
    /** Returns true if this account is eligible for a monthly coupon this month and hasn't received one yet. */
    public boolean needsMonthlyCoupon() {
        if (TIER_NONE.equals(tier)) return false;
        String thisMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return !thisMonth.equals(lastCouponMonth);
    }

    public double monthlyCouponValue() {
        if (TIER_BLACKGOLD.equals(tier)) return COUPON_BLACKGOLD;
        if (TIER_BRONZE.equals(tier) || TIER_GOLD.equals(tier)) return COUPON_BRONZE;
        return 0;
    }

    /** Mark that the monthly coupon has been issued for this month. */
    public void markCouponIssued() {
        this.lastCouponMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    // ── Discount ──────────────────────────────────────────────────────────────
    /** Service discount % (0 for NONE/BRONZE, 2 for GOLD, 5 for BLACKGOLD). */
    public double getDiscountPct() { return discountPct; }

    /** Apply discount to an amount: returns amount - (amount * discountPct / 100). */
    public double applyDiscount(double amount) {
        return amount * (1.0 - discountPct / 100.0);
    }

    // ── Presentation helpers ──────────────────────────────────────────────────
    public String tierBadge() {
        switch (tier) {
            case TIER_BLACKGOLD: return "\u2B50 BLACKGOLD";
            case TIER_GOLD:      return "\uD83E\uDD47 GOLD";
            case TIER_BRONZE:    return "\uD83E\uDD49 BRONZE";
            default:             return "  NONE";
        }
    }

    public java.awt.Color tierColor() {
        switch (tier) {
            case TIER_BLACKGOLD: return new java.awt.Color(255, 215,   0);  // gold-black
            case TIER_GOLD:      return new java.awt.Color(255, 200,  60);
            case TIER_BRONZE:    return new java.awt.Color(205, 127,  50);
            default:             return new java.awt.Color(160, 180, 210);
        }
    }

    // ── Legacy compat stubs (used in old VIP panel code, harmless) ────────────
    public double getLaborDiscountPct()   { return discountPct; }
    public int    getValetUsed()          { return 0; }
    public int    getValetQuota()         { return 0; }
    public boolean hasValetRemaining()    { return false; }
    public int    valetRemaining()        { return 0; }
    public void   useValet()              { }
    public boolean isBlackCard()          { return TIER_BLACKGOLD.equals(tier); }
    public boolean isSubscriptionActive() { return isBlackCard(); }
    public double partsDiscountPct()      { return 0.0; }
    public String getSubscriptionType()   { return subscriptionType; }
    public String getSubscriptionExpiry() { return subscriptionExpiry; }
    public void setSubscriptionType(String v)   { this.subscriptionType = v; }
    public void setSubscriptionExpiry(String v) { this.subscriptionExpiry = v; }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getVipId()            { return vipId; }
    public String getCustomerId()       { return customerId; }
    public String getPhone()            { return phone; }
    public int    getPoints()           { return points; }
    public double getBalance()          { return points; }
    public String getTier()             { return tier; }
    public String getCreatedDate()      { return createdDate; }
    public double getTotalSpent()       { return totalSpent; }
    public String getLastCouponMonth()  { return lastCouponMonth; }

    public void setPhone(String v)          { phone = v; }
    public void setPoints(int v)            { points = v; recalcTier(); }
    public void setTotalSpent(double v)     { totalSpent = v; }
    public void setLastCouponMonth(String v){ lastCouponMonth = v; }
    public void setValetUsed(int v)         { /* no-op */ }
    public void setValetQuota(int v)        { /* no-op */ }

    // ── Persistence ───────────────────────────────────────────────────────────
    /**
     * 12-field format (backward compatible with old FileManager reader):
     * vipId | customerId | phone | points | tier | createdDate |
     * totalSpent | discountPct | 0 | 0 | subType | lastCouponMonth
     *
     * We reuse field 12 (subscriptionExpiry) to store lastCouponMonth so no
     * schema change is needed in FileManager.
     */
    public String toFileString() {
        return vipId + "|" + customerId + "|" + phone + "|"
             + points + "|" + tier + "|" + createdDate + "|"
             + String.format("%.2f", totalSpent) + "|"
             + String.format("%.2f", discountPct) + "|"
             + "0|0|"
             + subscriptionType + "|"
             + (lastCouponMonth != null && !lastCouponMonth.isEmpty() ? lastCouponMonth : "N/A");
    }
}
