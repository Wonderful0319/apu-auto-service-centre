package asc.model;

public class VehicleHealthRecord {

    private String recordId;
    private String appointmentId;
    private String customerId;
    private String recordDate;
    private String tirePressure;
    private String brakeThickness;
    private String oilType;
    private String sparkPlugStatus;
    private int    mileage;
    private String notes;
    private String technicianId;

    public VehicleHealthRecord(String recordId, String appointmentId, String customerId,
                                String recordDate, String tirePressure, String brakeThickness,
                                String oilType, String sparkPlugStatus,
                                int mileage, String notes, String technicianId) {
        this.recordId        = recordId;
        this.appointmentId   = appointmentId;
        this.customerId      = customerId;
        this.recordDate      = recordDate;
        this.tirePressure    = tirePressure;
        this.brakeThickness  = brakeThickness;
        this.oilType         = oilType;
        this.sparkPlugStatus = sparkPlugStatus;
        this.mileage         = mileage;
        this.notes           = notes;
        this.technicianId    = technicianId;
    }

    public String getRecordId()        { return recordId; }
    public String getAppointmentId()   { return appointmentId; }
    public String getCustomerId()      { return customerId; }
    public String getRecordDate()      { return recordDate; }
    public String getTirePressure()    { return tirePressure; }
    public String getBrakeThickness()  { return brakeThickness; }
    public String getOilType()         { return oilType; }
    public String getSparkPlugStatus() { return sparkPlugStatus; }
    public int    getMileage()         { return mileage; }
    public String getNotes()           { return notes; }
    public String getTechnicianId()    { return technicianId; }

    public void setTirePressure(String v)    { tirePressure = v; }
    public void setBrakeThickness(String v)  { brakeThickness = v; }
    public void setOilType(String v)         { oilType = v; }
    public void setSparkPlugStatus(String v) { sparkPlugStatus = v; }
    public void setMileage(int v)            { mileage = v; }
    public void setNotes(String v)           { notes = v; }

    public String toFileString() {
        return recordId + "|" + appointmentId + "|" + customerId + "|"
             + recordDate + "|" + tirePressure + "|" + brakeThickness + "|"
             + oilType + "|" + sparkPlugStatus + "|" + mileage + "|"
             + notes + "|" + technicianId;
    }
}
