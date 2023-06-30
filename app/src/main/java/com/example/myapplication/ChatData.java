package com.example.myapplication;

import java.io.Serializable;


public class ChatData {

    private String msg;
    private String nickname;
    private boolean sentByMe;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }
    public void setSentByMe(boolean sentByMe) {
        this.sentByMe = sentByMe;
    }

}