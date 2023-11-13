package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import scc.utils.Helpers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Period {

    private String houseId;
    private int discount;

    private String startDate;

    private String endDate;

    public Period(){}

    public Period(String houseId, int discount, String startDate, String endDate) {
        this.houseId = houseId;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
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


    @Override
    public String toString() {
        return "Period{" +
                " houseId='" + houseId + '\'' +
                ", discount=" + discount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
