package at.aau.serg.scotlandyard.model;

import lombok.Setter;

public class User {
    @Setter
    private Long id;
    private String username;
    private String password;

    public User() {
    }

    public User(String username, String password) {

        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }

}
