package com.example.healthbot.data.entity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    private String address;

    public User(Long id) {
        this.id = id;
    }

    public User(String username, String address) {
        this.username = username;
        this.address = address;
    }

    public User() {

    }
}
