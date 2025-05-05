package com.msn.msncars.listing;

public enum ValidityPeriod {
    SHORT(7),
    STANDARD(14),
    EXTENDED(30);

    private final int numberOfDays;

    ValidityPeriod(int numberOfDays){
        this.numberOfDays = numberOfDays;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }
}
