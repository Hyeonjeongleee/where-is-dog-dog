package com.example.myapplication;

public class Chat {
    private String name;
    private String nickname;
    private String markerID;

    public Chat(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
    }

    public String getChatName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMarkerID() {
        return markerID;
    }
    public void setMarkerID(String markerId) {
        this.markerID = markerId;
    }
}
