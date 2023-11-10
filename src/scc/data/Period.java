package scc.data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Period {

    private String id;

    private String houseId;

    private double normalPrice;

    private double discountedPrice;
    private LocalDate startDate;
    private LocalDate endDate;

    private String[] rentalIds;

    public Period(){}

    public Period(String id, String houseId, double normalPrice, double discountedPrice, LocalDate startDate, LocalDate endDate, String[] rentalIds) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date should be before or equal to end date.");
        }
        this.id = id;
        this.houseId = houseId;
        this.normalPrice = normalPrice;
        this.discountedPrice = discountedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentalIds = rentalIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public double getNormalPrice() {
        return normalPrice;
    }

    public void setNormalPrice(double normalPrice) {
        this.normalPrice = normalPrice;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String[] getRentalIds() {
        return rentalIds;
    }

    public void setRentalIds(String[] rentalIds) {
        this.rentalIds = rentalIds;
    }

    public boolean intersects(Period other) {
        return !this.startDate.isAfter(other.endDate) && !other.startDate.isAfter(this.endDate);
    }

    @Override
    public String toString() {
        return "Period{" +
                "id='" + id + '\'' +
                ", houseId='" + houseId + '\'' +
                ", normalPrice=" + normalPrice +
                ", discountedPrice=" + discountedPrice +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", rentals=" + Arrays.toString(rentalIds) +
                '}';
    }
}
