package com.msn.msncars.car;

import jakarta.persistence.*;

@Entity
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "make_id")
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
