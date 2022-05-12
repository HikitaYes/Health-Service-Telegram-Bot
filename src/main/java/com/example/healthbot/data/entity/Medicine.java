package com.example.healthbot.data.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "fav_medicines")
@Getter
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long idUser;

    private String medicine;

    public Medicine(Long idUser, String medicine) {
        this.idUser = idUser;
        this.medicine = medicine;
    }

    private Medicine() {}
}
