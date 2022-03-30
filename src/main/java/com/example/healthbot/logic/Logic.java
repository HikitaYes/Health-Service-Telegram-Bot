package com.example.healthbot.logic;

import com.example.healthbot.HttpClient.HttpClient;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class Logic {
    private final String helpMsg = "Я бот, который покажет вам самые низкие цены на лекарства в аптеках Екатеринбурга.\nВведите название лекарства.";
    private final String startMsg = "Привет! " + helpMsg;
    private final String sorryMsg = "Такого лекарства я не нашел. Попробуйте еще раз.";
    private final String medicinesChoiceMsg = "Выберите производителя и дозировку:";
    private final String districtChoiceMsg = "Выберите район, в котором хотите найти аптеки:";

    private Map<String, String> districts = new LinkedHashMap<>();
    {
        districts.put("-1", "Все районы");
        districts.put("9", "Академический");
        districts.put("7", "Верх-Исетский");
        districts.put("5", "Железнодорожный");
        districts.put("6", "Кировский");
        districts.put("1", "Ленинский");
        districts.put("4", "Октябрьский");
        districts.put("2", "Орджоникидзевский");
        districts.put("3", "Чкаловский");
    }

    private Integer targetId = null;
    private final HttpClient httpClient;

    public Logic(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Answer getAnswer(String message) {
        switch (message) {
            case "/start":
                return new Answer.Text(startMsg);
            case "/help":
                return new Answer.Text(helpMsg);
            default:
                try {
                    var number = Integer.parseInt(message);
                    if (targetId != null) {
                        if (districts.containsKey(Integer.toString(number))) {
                            String info = findResultInfo(number);
                            return new Answer.SearchResult(info);
                        }
                        else {
                            targetId = number;
                            return new Answer.DistrictChoice(districtChoiceMsg, districts);
                        }
                    }
                    else {
                        targetId = number;
                        return new Answer.DistrictChoice(districtChoiceMsg, districts);
                    }
                } catch (NumberFormatException e) {
                    Map<String, String> medicines = findMedicines(message);
                    if (medicines.isEmpty())
                        return new Answer.Text(sorryMsg);
                    return new Answer.MedicinesChoice(medicinesChoiceMsg, medicines);
                }
        }
    }

    @SneakyThrows
    private Map<String, String> findMedicines(String name) {
        var headers = new HttpHeaders();
        headers.add("act", "go");
        headers.add("request", URLEncoder.encode(name, "cp1251"));

//        Mono<String> data = httpClient.getPage("/search.php", map);
//        data.subscribe(text -> {
        String text = httpClient.getPage("/search.php", headers);
        Map<String, String> info = new HashMap<>();
        Pattern pattern = Pattern.compile("<a href='/health/pharma/med-\\d+'>.+</a>");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            var match = matcher.group()
                    .split("<a href='/health/pharma/med-")[1]
                    .split("</a>")[0]
                    .split(">");
            var number = match[0].substring(0, match[0].length() - 1);
            var fullName = match[1];
            info.put(number, fullName);
        }
        return info;
    }

    private String findResultInfo(Integer district) {
        var headers = new HttpHeaders();
        headers.add("dist", district.toString());
        String text = httpClient.getPage("/med-%s".formatted(targetId), headers);

        var parse = Arrays.stream(text
                .split("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">")[1]
                .split("<table border=\"0\" cellspacing=\"0\" cellpadding=\"9\" width=\"100%\">")[0]
                .split("<tr valign=\"top\".+>")).skip(1)
                .toList();

        List<String> info = new ArrayList<>();
        for (var data : parse) {
            var name = data
                    .split("target=\"_blank\">")[1]
                    .split("</a>")[0]
                    .trim();
            var address = data
                    .split("<nobr>")[1]
                    .split("</nobr>")[0]
                    .trim()
                    .replaceAll("<a href=.+?>", "")
                    .replaceAll("</a>", "")
                    .split("Екатеринбург, ")[1];
            if (!address.split(" ")[0].equals("ул.") && !address.contains(".ru"))
                address = "ул. " + address;
            var cost = data
                    .split("<td style='text-align:right;'><span style=\"\">")[1]
                    .split("</span>")[0];
            info.add("%s\n%s, %s".formatted(cost, name, address));
        }
        return String.join("\n", info);
    }
}
