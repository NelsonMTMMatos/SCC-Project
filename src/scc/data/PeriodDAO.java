package scc.data;

import scc.utils.Helpers;

import java.util.UUID;

public class PeriodDAO {

    private String _rid;

    private String _ts;

    private String id;

    private String houseId;

    private int discount;

    private String startDate;

    private String endDate;

    public PeriodDAO(){}

    public PeriodDAO(Period p){
        this(p.getHouseId(), p.getDiscount(), p.getStartDate(), p.getEndDate());
    }

    public PeriodDAO(String houseId, int discount, String startDate, String endDate) {
        super();
        this.id = UUID.randomUUID().toString();
        this.houseId = houseId;
        this.discount = discount;
        this.startDate = Helpers.toISO8601String(startDate);
        this.endDate = Helpers.toISO8601String(endDate);
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

    public Period toPeriod(){
        return new Period(houseId, discount, startDate, endDate);
    }

    @Override
    public String toString() {
        return "Period{" +
                "id='" + id + '\'' +
                ", houseId='" + houseId + '\'' +
                ", discount=" + discount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
