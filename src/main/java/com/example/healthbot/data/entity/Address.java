package com.example.healthbot.data.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "fav_address")
@Getter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long idUser;

    private String address;

    public Address(Long idUser, String medicine) {
        this.idUser = idUser;
        this.address = medicine;
    }

    private Address() {}
}
