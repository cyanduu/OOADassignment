import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String plate;
    private String spotID;
    private long exitTime;
    private double amountPaid;
    private String paymentMethod; //Cash or Card

    public Transaction(String plate, String spotID, double amountPaid, String paymentMethod) {
        this.plate = plate;
        this.spotID = spotID;
        this.exitTime = System.currentTimeMillis();
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
    }

    //Getters
    public String getPlate() { return plate; }
    public String getSpotID() { return spotID; }
    public Date getExitTime() { return new Date(exitTime); }
    public double getAmount() { return amountPaid; }
    public String getMethod() { return paymentMethod; }
}