package com.msn.MSNcars.listing;

import com.msn.MSNcars.car.Feature;
import jakarta.persistence.*;

@Entity
public class ListingFeature {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne
    @JoinColumn(name = "feature_id")
    private Feature feature;

    public ListingFeature() {}

    public ListingFeature(Long id, Listing listing, Feature feature) {
        this.id = id;
        this.listing = listing;
        this.feature = feature;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
