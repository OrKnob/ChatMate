package com.example.chatmate.Model;

public class User {

    private String id;
    private String username;
    private String status;
    private String typing;

    public User(String id, String username, String status,String typing) {
        this.id = id;
        this.username = username;
        this.status = status;
        this.typing = typing;
    }

    public User() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTyping() {
        return typing;
    }

    public void setTyping(String typing) {
        this.typing = typing;
    }
}
