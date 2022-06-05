package com.example.chatsapp.Modals;

public class User {
    private  String uid,name,phoneNumber,profileimg,token;

    public User(){

    }

    public User(String uid, String name, String phoneNumber, String profileimg) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileimg = profileimg;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokken) {
        this.token = tokken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumeber() {
        return phoneNumber;
    }

    public void setPhoneNumeber(String phoneNumeber) {
        this.phoneNumber = phoneNumeber;
    }

    public String getProfileimg() {
        return profileimg;
    }

    public void setProfileimg(String profileimg) {
        this.profileimg = profileimg;
    }
}

