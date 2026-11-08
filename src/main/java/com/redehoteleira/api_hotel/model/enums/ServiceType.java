package com.redehoteleira.api_hotel.model.enums;

public enum ServiceType {
    ROOM_SERVICE(50.0),
    LAUNDRY(30.0),
    BREAKFAST(40.0),
    PARKING(20.0);

    private final Double defaultPrice;

    ServiceType(Double defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public Double getDefaultPrice() {
        return defaultPrice;
    }
}