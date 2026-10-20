package asc.model;

/**
 * Model class representing a service appointment.
 *
 * STATUS LIFECYCLE:
 *   SCHEDULED       → Staff created & technician assigned
 *   IN_PROGRESS     → Technician has started the job
 *   AWAITING_PAYMENT→ Technician marked job done; waiting for customer payment at counter
 *   COMPLETED       → Staff confirmed payment received; receipt generated
 */
public class Appointment {
  // ── Status constants (single source of truth) ──────────────────────────────
  public static final String STATUS_SCHEDULED        = "SCHEDULED";
  public static final String STATUS_IN_PROGRESS      = "IN_PROGRESS";
  public static final String STATUS_AWAITING_PAYMENT = "AWAITING_PAYMENT";
  public static final String STATUS_COMPLETED        = "COMPLETED";

  private String appointmentId;
  private String customerId;
  private String technicianId;
  private String counterStaffId;
  private String serviceType;
  private String status;
  private String date;
  private String time;
  private String vehicleInfo;
  private String notes;

  public Appointment(String appointmentId, String customerId, String technicianId,
                     String counterStaffId, String serviceType, String status,
                     String date, String time, String vehicleInfo, String notes) {
    this.appointmentId  = appointmentId;
    this.customerId     = customerId;
    this.technicianId   = technicianId;
    this.counterStaffId = counterStaffId;
    this.serviceType    = serviceType;
    this.status         = status;
    this.date           = date;
    this.time           = time;
    this.vehicleInfo    = vehicleInfo;
    this.notes          = notes;
  }

  public String getAppointmentId()  { return appointmentId; }
  public String getCustomerId()     { return customerId; }
  public String getTechnicianId()   { return technicianId; }
  public void   setTechnicianId(String technicianId) { this.technicianId = technicianId; }
  public String getCounterStaffId() { return counterStaffId; }
  public void   setCounterStaffId(String counterStaffId) { this.counterStaffId = counterStaffId; }
  public String getServiceType()    { return serviceType; }
  public void   setServiceType(String serviceType) { this.serviceType = serviceType; }
  public String getStatus()         { return status; }
  public void   setStatus(String status) { this.status = status; }
  public String getDate()           { return date; }
  public void   setDate(String date){ this.date = date; }
  public String getTime()           { return time; }
  public void   setTime(String time){ this.time = time; }
  public String getVehicleInfo()    { return vehicleInfo; }
  public String getNotes()          { return notes; }
  public void   setNotes(String notes) { this.notes = notes; }

  /** True if the technician can still change the status (not yet paid). */
  public boolean isEditable() {
    return STATUS_SCHEDULED.equals(status) || STATUS_IN_PROGRESS.equals(status);
  }

  /** True if payment can be collected by counter staff. */
  public boolean isAwaitingPayment() {
    return STATUS_AWAITING_PAYMENT.equals(status);
  }

  /** True if the whole lifecycle is done. */
  public boolean isCompleted() {
    return STATUS_COMPLETED.equals(status);
  }

  /** Human-readable label for display in tables. */
  public String getStatusLabel() {
    switch (status) {
      case STATUS_SCHEDULED:        return "Scheduled";
      case STATUS_IN_PROGRESS:      return "In Progress";
      case STATUS_AWAITING_PAYMENT: return "Awaiting Payment";
      case STATUS_COMPLETED:        return "Completed";
      default:                      return status;   // backward-compat with old PENDING/COMPLETED data
    }
  }

  /** Returns the duration of the service in hours based on type. */
  public int getDurationHours() {
    return serviceType.equalsIgnoreCase("NORMAL") ? 1 : 3;
  }

  /** Returns pipe-separated string for file storage. */
  public String toFileString() {
    return appointmentId + "|" + customerId + "|" + technicianId + "|" + counterStaffId + "|" +
           serviceType + "|" + status + "|" + date + "|" + time + "|" + vehicleInfo + "|" + notes;
  }

  @Override
  public String toString() {
    return appointmentId + " | " + serviceType + " | " + getStatusLabel() + " | " + date + " " + time;
  }
}
