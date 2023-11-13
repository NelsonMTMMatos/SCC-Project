package scc.data;

import java.util.Arrays;
import java.util.UUID;

public class HouseDAO {

    private String _rid;
    private String _ts;
    private String id;
    private String name;
    private String location;
    private String description;
    private double price;
    private String[] photoIds;
    private String ownerId;

    public HouseDAO(){}

    public HouseDAO(House h){ this(h.getName(), h.getLocation(), h.getDescription(), h.getPrice(), h.getPhotoIds(), h.getOwnerId()); }

    public HouseDAO(String name, String location, String description, double price, String[] photoIds, String ownerId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.description = description;
        this.price = price;
        this.photoIds = photoIds;
        this.ownerId = ownerId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    public String[] getPhotoIds() {
        return photoIds == null ? new String[0] : photoIds;
    }

    public void setPhotoIds(String[] photoIds) {
        this.photoIds = photoIds;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public House toHouse(){
        return new House(name, location, description, price, photoIds, ownerId);
    }

    @Override
    public String toString() {
        return "House{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", photoIds='" + Arrays.toString(photoIds) + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }
}
