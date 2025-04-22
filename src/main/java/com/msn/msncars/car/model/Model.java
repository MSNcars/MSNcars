package com.msn.msncars.car.model;

import com.msn.msncars.car.make.Make;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "make_id")
    @NotNull
    private Make make;

    public Model() {}

    public Model(Long id, String name, Make make) {
        this.id = id;
        this.name = name;
        this.make = make;
    }

    public Model(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMake(Make make) {
        this.make = make;
    }

    public Make getMake() {
        return make;
    }
}
