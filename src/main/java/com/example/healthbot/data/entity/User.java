package com.example.healthbot.data.entity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Getter
public class User {
    @Id
    private Long id;

    private String username;

    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public User() {}
}
