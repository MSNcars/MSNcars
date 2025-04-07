package com.msn.msncars.listing;

import com.msn.msncars.company.Company;
import com.msn.msncars.car.*;
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
    @JoinColumn(name = "selling_company_id")
    private Company sellingCompany;

    @ManyToOne
    @JoinColumn(name = "make_id")
    private Make make;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private Model model;

    @ManyToMany
    @JoinTable(
        name="listing_feature",
        joinColumns = @JoinColumn(name = "listing_id"),
        inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private List<Feature> features;

    private LocalDate createdAt;
    private LocalDate expiresAt;

    private Boolean revoked;

    @Min(value = 0)
    private BigDecimal price;

    @Min(value = 1900)
    private Integer productionYear;

    @Min(value = 0)
    private Integer mileage;

    @Enumerated(EnumType.STRING)
    private Fuel fuel;

    @Enumerated(EnumType.STRING)
    private CarUsage carUsage;

    @Enumerated(EnumType.STRING)
    private CarOperationalStatus carOperationalStatus;

    @Enumerated(EnumType.STRING)
    private CarType carType;

    @Size(max = 500)
    private String description;

    public Listing() {}

    public Listing(Long id, String ownerId, Company sellingCompany, Make make, Model model, List<Feature> features, LocalDate createdAt, LocalDate expiresAt, Boolean revoked, BigDecimal price, Integer productionYear, Integer mileage, Fuel fuel, CarUsage carUsage, CarOperationalStatus carOperationalStatus, CarType carType, String description) {
        this.id = id;
        this.ownerId = ownerId;
        this.sellingCompany = sellingCompany;
        this.make = make;
        this.model = model;
        this.features = features;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.price = price;
        this.productionYear = productionYear;
        this.mileage = mileage;
        this.fuel = fuel;
        this.carUsage = carUsage;
        this.carOperationalStatus = carOperationalStatus;
        this.carType = carType;
        this.description = description;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
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

    public Company getSellingCompany() {
        return sellingCompany;
    }

    public void setSellingCompany(Company sellingCompany) {
        this.sellingCompany = sellingCompany;
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

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
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

    public Fuel getFuel() {
        return fuel;
    }

    public void setFuel(Fuel fuel) {
        this.fuel = fuel;
    }

    public CarUsage getCarUsage() {
        return carUsage;
    }

    public void setCarUsage(CarUsage carUsage) {
        this.carUsage = carUsage;
    }

    public CarOperationalStatus getCarOperationalStatus() {
        return carOperationalStatus;
    }

    public void setCarOperationalStatus(CarOperationalStatus carOperationalStatus) {
        this.carOperationalStatus = carOperationalStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
