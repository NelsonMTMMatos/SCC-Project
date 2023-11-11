package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.Period;

public class Rental {
    private String id;
    private String houseId;
    private String userId;

    private String startDate;

    private String endDate;
    private int price;

    public Rental(){}

    public Rental(String id, String houseId, String userId, String startDate, String endDate, int price) {
        this.id = id;
        this.houseId = houseId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
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
}
