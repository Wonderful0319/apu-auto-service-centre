package asc.model;

/**
 * Pending registration awaiting manager approval.
 * status: PENDING / APPROVED / REJECTED
 */
public class PendingRegistration {
    private String regId;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String vehicleInfo;   // only for CUSTOMER role
    private String role;          // CUSTOMER / COUNTER_STAFF / TECHNICIAN / MANAGER
    private String status;        // PENDING / APPROVED / REJECTED
    private String appliedDate;
    private String reviewedBy;    // managerId, empty if pending
    private String reviewedDate;

    public PendingRegistration(String regId, String username, String password,
                               String name, String email, String phone,
                               String gender, String vehicleInfo, String role,
                               String status, String appliedDate,
                               String reviewedBy, String reviewedDate) {
        this.regId        = regId;
        this.username     = username;
        this.password     = password;
        this.name         = name;
        this.email        = email;
        this.phone        = phone;
        this.gender       = gender;
        this.vehicleInfo  = vehicleInfo;
        this.role         = role;
        this.status       = status;
        this.appliedDate  = appliedDate;
        this.reviewedBy   = reviewedBy;
        this.reviewedDate = reviewedDate;
    }

    public String getRegId()        { return regId; }
    public String getUsername()     { return username; }
    public String getPassword()     { return password; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public String getGender()       { return gender; }
    public String getVehicleInfo()  { return vehicleInfo; }
    public String getRole()         { return role; }
    public String getStatus()       { return status; }
    public void   setStatus(String s){ this.status = s; }
    public String getAppliedDate()  { return appliedDate; }
    public String getReviewedBy()   { return reviewedBy; }
    public void   setReviewedBy(String r){ this.reviewedBy = r; }
    public String getReviewedDate() { return reviewedDate; }
    public void   setReviewedDate(String d){ this.reviewedDate = d; }

    public String toFileString() {
        return regId + "|" + username + "|" + password + "|"
             + name + "|" + email + "|" + phone + "|"
             + gender + "|" + vehicleInfo + "|" + role + "|"
             + status + "|" + appliedDate + "|"
             + reviewedBy + "|" + reviewedDate;
    }
}
