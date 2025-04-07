package com.msn.msncars.listing;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    public ListingService(ListingRepository listingRepository, ListingMapper listingMapper) {
        this.listingRepository = listingRepository;
        this.listingMapper = listingMapper;
    }

    public List<ListingResponse> getAllListings() {
        List<Listing> listings = listingRepository.findAll();
        List<ListingResponse> listingResponses = new ArrayList<>();

        for (Listing listing : listings) {
            listingResponses.add(listingMapper.fromListing(listing));
        }

        return listingResponses;
    }

    public ListingResponse getListingById(Long listingId) {
        Optional<Listing> listing = listingRepository.findById(listingId);

        return listingMapper.fromListing(
                listing.orElseThrow(() -> new IllegalArgumentException("There is no listing with that id.")));
    }

    public Long createListing(ListingRequest listingRequest) {
        var listing = listingRepository.save(listingMapper.toListing(listingRequest));
        return listing.getId();
    }
}
