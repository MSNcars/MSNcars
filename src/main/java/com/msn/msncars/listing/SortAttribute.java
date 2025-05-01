package com.msn.msncars.listing;

import java.util.Comparator;

public enum SortAttribute {
    PRICE(Comparator.comparing(Listing::getPrice)),
    MILEAGE(Comparator.comparing(Listing::getMileage));

    private final Comparator<Listing> comparator;

    SortAttribute(Comparator<Listing> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Listing> getComparator(SortOrder order) {
        return order == SortOrder.ASCENDING ? comparator : comparator.reversed();
    }
}
