package com.msn.msncars.listing;

import com.msn.msncars.listing.DTO.ListingRequest;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.exception.ListingExpirationDateException;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
                listing.orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId)));
    }

    /*
    public List<ListingResponse> getAllListingFromUser(String userId) {
        List<Listing> listings = listingRepository.findAllByOwnerId(userId);
        List<ListingResponse> listingResponses = new ArrayList<>();

        for (Listing listing : listings) {
            listingResponses.add(listingMapper.fromListing(listing));
        }

        return listingResponses;
    }
     */

    public Long createListing(ListingRequest listingRequest) {
        Listing listing = listingRepository.save(listingMapper.toListing(listingRequest));

        return listing.getId();
    }

    public ListingResponse updateListing(Long listingId, ListingRequest listingRequest) {
        Listing existingListing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        Listing updatedListing = listingMapper.toListing(listingRequest);
        updatedListing.setId(listingId);

        Listing savedListing = listingRepository.save(updatedListing);

        return listingMapper.fromListing(savedListing);
    }

    public ListingResponse extendExpirationDate(Long listingId, LocalDate newExpirationDate) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        if (newExpirationDate.isBefore(LocalDate.now())) {
            throw new ListingExpirationDateException("New expiration date cannot be in the past");
        }

        listing.setExpiresAt(newExpirationDate);
        Listing updatedListing = listingRepository.save(listing);

        return listingMapper.fromListing(updatedListing);
    }

    public void deleteListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with id: " + listingId));

        listingRepository.delete(listing);
    }
}
