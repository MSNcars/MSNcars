package com.msn.msncars.listing;

public enum ValidityPeriod {
    Short(7),
    Standard(14),
    Extended(30);

    final int numberOfDays;

    ValidityPeriod(int numberOfDays){
        this.numberOfDays = numberOfDays;
    }
}
