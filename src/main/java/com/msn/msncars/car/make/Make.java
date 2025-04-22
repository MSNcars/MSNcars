package com.msn.msncars.car.make;

import com.msn.msncars.car.model.Model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Entity
public class Make {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @OneToMany(mappedBy = "make")
    List<Model> models;

    public Make() {}

    public Make(Long id, String name, List<Model> models) {
        this.id = id;
        this.name = name;
        this.models = models;
    }

    public Make(Long id, String name) {
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

    public void setModels(List<Model> models) {
        this.models = models;
    }

    public List<Model> getModels() {
        return models;
    }
}
