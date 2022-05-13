package com.example.healthbot.logic;

import com.example.healthbot.httpclient.HttpClient;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class Logic {
    private final String KeyYandexMapsApi = "key here";

    private final String helpMsg = "Я бот, который покажет вам самые низкие цены на лекарства в аптеках Екатеринбурга.\nВведите название лекарства.";
    private final String startMsg = "Привет! " + helpMsg;
    private final String sorryMsg = "Такого лекарства я не нашел. Попробуйте еще раз.";
    private final String medicinesChoiceMsg = "Выберите производителя и дозировку:";
    private final String addressChoiceMsg = "Введите адрес в формате \"Улица дом\", около которого хотите найти аптеки, или нажмите на выбор района";
    private final String districtChoiceMsg = "Выберите район из списка:";
    private final String errorMsg = "Что-то пошло не так, попробуйте еще раз.";

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
    private State state = new State.ExpectMedicineName();

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
                    switch (state) {
                        case State.ExpectMedicineId m -> {
                            state = new State.ExpectAddress();
                            targetId = number;
                            return new Answer.AddressChoice(addressChoiceMsg);
                        }
                        case State.ExpectDistrict d -> {
                            if (districts.containsKey(Integer.toString(number))) {
                                state = new State.ExpectMedicineName();
                                List<String> info = findResultInfo(Integer.toString(number));
                                return new Answer.SearchResult(String.join("\n", info));
                            } else {
                                return new Answer.Text(districtChoiceMsg);
                            }
                        }
                        default -> {
                            state = new State.ExpectMedicineName();
                            return new Answer.Text(errorMsg);
                        }
                    }
                } catch (NumberFormatException e) {
                    switch (state) {
                        case State.ExpectMedicineName m -> {
                            state = new State.ExpectMedicineId();
                            Map<String, String> medicines = findMedicines(message);
                            if (medicines.isEmpty())
                                return new Answer.Text(sorryMsg);
                            return new Answer.MedicinesChoice(medicinesChoiceMsg, medicines);
                        }
                        case State.ExpectAddress a -> {
                            if (message.equals("Выбор района")) {
                                state = new State.ExpectDistrict();
                                return new Answer.DistrictChoice(districtChoiceMsg, districts);
                            }
                            state = new State.ExpectMedicineName();
                            var result = getNearestResults(message);
                            return new Answer.Text(result);
                        }
                        default -> {
                            return new Answer.Text(errorMsg);
                        }
                    }
                }
        }
    }

    private String readFile(String name) {
        String baseName = "D:\\Intelij IDEA\\Naumen\\Health-Service-Telegram-Bot\\src\\main\\resources\\html\\";
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(
                    baseName + name + ".html", Charset.forName("Cp1251")));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
        }
        String content = contentBuilder.toString();
        return content;
    }

    @SneakyThrows
    private Map<String, String> findMedicines(String name) {
//        var headers = new HttpHeaders();
//        headers.add("act", "go");
//        headers.add("request", URLEncoder.encode(name, "cp1251"));

//        String text = httpClient.getPage("/search.php", headers);
        String text = readFile(name);

        Map<String, String> info = new HashMap<>();
        Pattern pattern = Pattern.compile("/health/pharma/med-\\d+[\"']>.+?</a>");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            var match = matcher.group()
                    .split("/health/pharma/med-")[1]
                    .split("</a>")[0]
                    .split(">");
            var number = match[0].substring(0, match[0].length() - 1);
            var fullName = match[1];
            info.put(number, fullName);
        }
        return info;
    }

    private List<String> findResultInfo(String district) {
//        var headers = new HttpHeaders();
//        headers.add("dist", district);
//        String text = httpClient.getPage("/med-%s".formatted(targetId), headers);

        String text = readFile(targetId + "\\" + district);

        var parse = Arrays.stream(text
                .split("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">")[1]
                .split("<table border=\"0\" cellspacing=\"0\" cellpadding=\"9\" width=\"100%\">")[0]
                .split("<tr valign=\"top\".+?>")).skip(1)
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
                    .replaceAll("</a>", "");

            var town = address.split(", ")[0];
            if (!town.equals("Екатеринбург")) continue;
            address = address.split("Екатеринбург, ")[1];

            if (!address.split(" ")[0].equals("ул.") && !address.contains(".ru"))
                address = "ул. " + address;
            var cost = data
                    .split("<td style='text-align:right;'><span style=\"\">")[1]
                    .split("</span>")[0];
            info.add("%s\n%s, %s".formatted(cost, name, address));
        }
        return info;
    }

    private String getCoordinates(String address) {
        String query = String.format(
                "https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&geocode=Екатеринбург,%s",
                KeyYandexMapsApi, address);

        var response = WebClient.create().get().uri(query).retrieve().bodyToMono(String.class).block();
        var coordinates = response.split("Point")[1].split("pos\":\"")[1].split("\"")[0];
        return coordinates;
    }

    private String getDistrict(String coordinates) {
        coordinates = coordinates.replace(" ", ",");
        String query = String.format(
                "https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&geocode=%s&kind=district",
                KeyYandexMapsApi, coordinates);

        var response = WebClient.create().get().uri(query).retrieve().bodyToMono(String.class).block();
        var parse = response.split("\"kind\":\"district\",\"name\":\"");
        for (int i = 1; i < parse.length; i++) {
            var district = parse[i].split("\"}]}")[0];
            if (!district.contains(" район")) continue;
            district = district.split(" район")[0];
            return district;
        }
        return null;
    }

    private static List<Double> coordinateToDouble(String coordinate) {
        var list = coordinate.split(" ");
        return List.of(Double.parseDouble(list[0]), Double.parseDouble(list[1]));
    }

    private static Double getDistance(String coord1, String coord2) {
        var coordinate1 = coordinateToDouble(coord1);
        var coordinate2 = coordinateToDouble(coord2);
        var x1 = coordinate1.get(0);
        var x2 = coordinate2.get(0);
        var y1 = coordinate1.get(1);
        var y2 = coordinate2.get(1);
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    private String getNearestResults(String initialAddress) {
        var initialCoordinates = getCoordinates(initialAddress);
        var district = getDistrict(initialCoordinates);
        var number = districts
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(district))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("-1");
        var info = findResultInfo(number);

        info = info.stream()
            .map(x -> {
                var address = x.split("ул. ")[1];
                var coordinates = getCoordinates(address);
                Double distance = getDistance(initialCoordinates, coordinates);
                return Pair.of(distance, x);
            })
            .sorted(Comparator.comparing(Pair::getFirst))
            .map(Pair::getSecond)
            .toList();
        return String.join("\n", info);
    }
}
