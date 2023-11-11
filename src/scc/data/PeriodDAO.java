package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Arrays;

public class PeriodDAO {

    private String _rid;

    private String _ts;

    private String id;

    private String houseId;

    private double normalPrice;

    private double discountedPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String[] rentalIds;

    public PeriodDAO(){
    }

    public PeriodDAO(Period p, String id){
        this(p.getId(), p.getHouseId(), p.getNormalPrice(), p.getDiscountedPrice(), p.getStartDate(), p.getEndDate(), p.getRentalIds());
    }

    public PeriodDAO(String id, String houseId, double normalPrice, double discountedPrice, LocalDate startDate, LocalDate endDate, String[] rentalIds) {
        super();
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

    public boolean intersects(PeriodDAO other) {
        return !this.startDate.isAfter(other.endDate) && !other.startDate.isAfter(this.endDate);
    }

    public Period toPeriod(){
        return new Period(id, houseId, normalPrice, discountedPrice, startDate, endDate, rentalIds == null ? null : Arrays.copyOf(rentalIds,rentalIds.length));
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
