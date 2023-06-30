package com.example.myapplication;

public class Chat {
    private String name;
    private String nickname;

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
}
