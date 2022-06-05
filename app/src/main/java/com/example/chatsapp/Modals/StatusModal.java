package com.example.chatsapp.Modals;

public class StatusModal {
    private String imgUrl;
    private Long timeStamp;

    public StatusModal(){

    }

    public StatusModal(String imgUrl, Long timeStamp) {
        this.imgUrl = imgUrl;
        this.timeStamp = timeStamp;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

}
