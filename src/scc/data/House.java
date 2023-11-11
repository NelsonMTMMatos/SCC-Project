package scc.data;

import java.util.Arrays;

public class House {

    private String id;
    private String name;
    private String location;
    private String description;
    private String[] photoIds;
    private String ownerId;

    public House() {}

    public House(String id, String name, String location, String description, String[] photoIds, String ownerId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.photoIds = photoIds;
        this.ownerId = ownerId;
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

    @Override
    public String toString() {
        return "House{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", photoIds='" + Arrays.toString(photoIds) + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }


}
