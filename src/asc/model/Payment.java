package asc.model;

public class Payment {
    private String paymentId;
    private String appointmentId;
    private String customerId;
    private double amount;
    private String paymentDate;
    private String paymentMethod;
    private String receiptNumber;

    public Payment(String paymentId, String appointmentId, String customerId, double amount,
                   String paymentDate, String paymentMethod, String receiptNumber) {
        this.paymentId = paymentId;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
    }

    public String getPaymentId() { return paymentId; }
    public String getAppointmentId() { return appointmentId; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReceiptNumber() { return receiptNumber; }

    public String toFileString() {
        return paymentId + "|" + appointmentId + "|" + customerId + "|" + amount + "|" + paymentDate + "|" + paymentMethod + "|" + receiptNumber;
    }

    @Override
    public String toString() {
        return receiptNumber + " | " + appointmentId + " | RM" + String.format("%.2f", amount) + " | " + paymentDate;
    }
}
