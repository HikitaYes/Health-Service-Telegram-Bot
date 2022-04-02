package com.example.healthbot.logic;

import java.util.Map;

public sealed interface Answer {
    record Text(String text) implements Answer {}
    record MedicinesChoice(String text, Map<String, String> medicines) implements Answer {}
    record DistrictChoice(String text, Map<String, String> districts) implements Answer {}
    record SearchResult(String info) implements Answer {}
}
