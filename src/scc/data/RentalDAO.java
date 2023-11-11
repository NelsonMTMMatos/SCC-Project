package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;

public class RentalDAO {

    private String _rid;
    private String _ts;
    private String id;
    private String houseId;
    private String userId;

    private String startDate;

    private String endDate;
    private int price;

    public RentalDAO() {}

    public RentalDAO(Rental r) {
        this(r.getId(), r.getHouseId(), r.getUserId(), r.getStartDate(), r.getEndDate(), r.getPrice());
    }

    public RentalDAO(String id, String houseId, String userId, String startDate, String endDate, int price) {
        super();
        this.id = id;
        this.houseId = houseId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
    }

    public String get_rid() {
        return _rid;
    }

    public void set_rid(String _rid) {
        this._rid = _rid;
    }

    public String get_ts() {
        return _ts;
    }

    public void set_ts(String _ts) {
        this._ts = _ts;
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

    public Rental toRental(){
        return new Rental(id, houseId, userId, startDate, endDate, price);
    }

    @Override
    public String toString() {
        return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", houseId=" + houseId + ", userId=" + userId
                + ", startDate=" + startDate + ", endDate=" + endDate + ", price=" + price + "]";
    }

}
