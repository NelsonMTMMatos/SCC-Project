package scc.data;

import java.util.UUID;

public class RentalDAO {

    private String _rid;

    private String _ts;

    private String id;

    private String houseId;

    private String userId;

    private String endDate;

    private String startDate;

    private double price;

    public RentalDAO() {}

    public RentalDAO(Rental r) {
        this(r.getUserId(), r.getStartDate(), r.getEndDate());
    }

    public RentalDAO(String userId, String startDate, String endDate) {
        super();
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Rental toRental(){
        return new Rental(userId, startDate, endDate);
    }

    @Override
    public String toString() {
        return "RentalDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", houseId=" + houseId + ", userId=" + userId
                + ", startDate=" + startDate + ", endDate=" + endDate + ", price=" + price + "]";
    }

}
