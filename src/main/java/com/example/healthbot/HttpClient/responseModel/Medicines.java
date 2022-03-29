package com.example.healthbot.HttpClient.responseModel;

import lombok.Data;

// Maybe to use, but request will be not to rest api, and we get raw html, not object type
@Data
public class Medicines {
    private String name;
    private Float price;
    private String address;
}

