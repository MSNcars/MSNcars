package com.msn.MSNcars.listing;

import com.msn.MSNcars.account.Company;
import com.msn.MSNcars.car.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ownerId;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "make_id")
    private Make make;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private Model model;

    @ManyToOne
    @JoinColumn(name = "car_type")
    private Type type;

    @ManyToMany
    @JoinTable(name="listing_feature")
    private List<Feature> features;

    private LocalDate createdAt;
    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    private ListingStatus listingStatus;

    @Min(value = 0)
    private BigDecimal price;

    @Min(value = 1900)
    private Integer productionYear;

    @Min(value = 0)
    private Integer mileage;

    @Enumerated(EnumType.STRING)
    private Fuel fuel;

    @Enumerated(EnumType.STRING)
    private CarCondition carCondition;

    @Size(max = 500)
    private String description;

    public Listing() {}

    public Listing(
            Long id,
            String ownerId,
            Company company,
            Make make,
            Model model,
            LocalDate createdAt,
            LocalDate expiresAt,
            ListingStatus listingStatus,
            BigDecimal price,
            Integer productionYear,
            Integer mileage,
            Fuel fuel,
            Type type,
            List<Feature> features,
            CarCondition carCondition,
            String description) {
        this.id = id;
        this.ownerId = ownerId;
        this.company = company;
        this.make = make;
        this.model = model;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.listingStatus = listingStatus;
        this.price = price;
        this.productionYear = productionYear;
        this.mileage = mileage;
        this.fuel = fuel;
        this.type = type;
        this.features = features;
        this.carCondition = carCondition;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public Make getMake() {
        return make;
    }

    public void setMake(Make make) {
        this.make = make;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public ListingStatus getListingStatus() {
        return listingStatus;
    }

    public void setListingStatus(ListingStatus listingStatus) {
        this.listingStatus = listingStatus;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(Integer productionYear) {
        this.productionYear = productionYear;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CarCondition getCarCondition() {
        return carCondition;
    }

    public void setCarCondition(CarCondition carCondition) {
        this.carCondition = carCondition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Fuel getFuel() {
        return fuel;
    }

    public void setFuel(Fuel fuel) {
        this.fuel = fuel;
    }
}
