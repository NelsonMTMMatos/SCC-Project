package scc.data;

import java.time.LocalDate;
import java.time.Period;

public class Rental {
    private String rentalId;
    private String houseId;
    private String userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int price;

    public Rental(String rentalId, String houseId, String userId, LocalDate startDate, LocalDate endDate, int price) {
        this.rentalId = rentalId;
        this.houseId = houseId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
    }

    public String getRentalId() {
        return rentalId;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
