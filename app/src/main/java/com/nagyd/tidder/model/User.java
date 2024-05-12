package com.nagyd.tidder.model;

import java.util.Map;
import java.util.Objects;

public class User {
    public String username;
    public String email;

    public User() {}

    public User(String name, String email) {
        this.username = name;
        this.email = email;
    }
}
