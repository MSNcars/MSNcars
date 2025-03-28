package com.msn.MSNcars.listing;

import com.msn.MSNcars.account.Firm;
import com.msn.MSNcars.car.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ownerId;

    @ManyToOne
    @JoinColumn(name = "firm_id")
    private Firm firm;

    @ManyToOne
    @JoinColumn(name = "make_id")
    private Make make;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private Model model;

    private LocalDate createdAt;
    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    private ListingStatus listingStatus;

    private BigDecimal price;
    private Integer productionYear;
    private Integer mileage;
    private Fuel fuel;

    @ManyToOne
    @JoinColumn(name = "car_type")
    private Type type;

    @Enumerated(EnumType.STRING)
    private CarCondition carCondition;

    @Size(max = 1000)
    private String description;

    public Listing() {}

    public Listing(
            Long id,
            Long ownerId,
            Firm firm,
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
            CarCondition carCondition,
            String description) {
        this.id = id;
        this.ownerId = ownerId;
        this.firm = firm;
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
        this.carCondition = carCondition;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Firm getFirm() {
        return firm;
    }

    public void setFirm(Firm firm) {
        this.firm = firm;
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
