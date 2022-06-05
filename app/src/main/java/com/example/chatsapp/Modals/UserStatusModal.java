package com.example.chatsapp.Modals;

import java.util.ArrayList;

public class UserStatusModal {
    private String name,profileImg;
    private Long lastUpdated;
    private ArrayList<StatusModal> statuses;

    public UserStatusModal(){

    }

    public UserStatusModal(String name, String profileImg, Long lastUpdated, ArrayList<StatusModal> statuses) {
        this.name = name;
        this.profileImg = profileImg;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public ArrayList<StatusModal> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<StatusModal> statuses) {
        this.statuses = statuses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
