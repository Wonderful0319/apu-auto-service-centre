package asc.model;

/**
 * Records a car part that a Technician marked as used for a specific appointment.
 * Stored in data/appointment_parts.txt  (pipe-delimited):
 *   appointmentId | partId | partName | unitPrice | qty
 */
public class AppointmentPart {

    private String appointmentId;
    private String partId;
    private String partName;
    private double unitPrice;
    private int    qty;

    public AppointmentPart(String appointmentId, String partId,
                            String partName, double unitPrice, int qty) {
        this.appointmentId = appointmentId;
        this.partId        = partId;
        this.partName      = partName;
        this.unitPrice     = unitPrice;
        this.qty           = qty;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPartId()        { return partId; }
    public String getPartName()      { return partName; }
    public double getUnitPrice()     { return unitPrice; }
    public int    getQty()           { return qty; }
    public double getSubtotal()      { return unitPrice * qty; }

    public String toFileString() {
        return appointmentId + "|" + partId + "|"
             + partName.replace("|", "/") + "|"
             + String.format("%.2f", unitPrice) + "|" + qty;
    }

    public static AppointmentPart fromLine(String line) {
        String[] p = line.split("\\|", 5);
        if (p.length < 5) return null;
        try {
            return new AppointmentPart(p[0].trim(), p[1].trim(), p[2].trim(),
                    Double.parseDouble(p[3].trim()), Integer.parseInt(p[4].trim()));
        } catch (Exception e) { return null; }
    }
}
