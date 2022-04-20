package com.example.healthbot.logic;

public sealed interface State {
    record ExpectMedicineName() implements State {}
    record ExpectMedicineId() implements State {}
    record ExpectDistrictOrAddress() implements State {}
}
