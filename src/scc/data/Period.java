package scc.data;

public class Period {

    private int discount;

    private String startDate;

    private String endDate;

    public Period(){}

    public Period(int discount, String startDate, String endDate) {
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
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
                "discount=" + discount +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}
