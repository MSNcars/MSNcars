package com.msn.msncars.listing;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
