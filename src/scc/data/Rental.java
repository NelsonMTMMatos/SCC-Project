package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import scc.utils.Helpers;

import java.time.LocalDate;
import java.time.Period;

public class Rental {
    private String houseId;
    private String userId;

    private String startDate;

    private String endDate;
    private int price;

    public Rental(){}

    public Rental(String houseId, String userId, String startDate, String endDate, int price) {
        this.houseId = houseId;
        this.userId = userId;
        this.startDate = Helpers.toISO8601String(startDate);
        this.endDate = Helpers.toISO8601String(endDate);
        this.price = price;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Rental [ houseId=" + houseId + ", userId=" + userId
                + ", startDate=" + startDate + ", endDate=" + endDate + ", price=" + price + "]";
    }
}
