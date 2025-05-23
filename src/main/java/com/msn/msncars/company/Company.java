package com.msn.msncars.company;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerId;
    private String name;
    private String address;
    private String phone;
    private String email;

    @ElementCollection
    @CollectionTable(name = "company_user")
    @Column(name = "user_id")
    private Set<String> members = new HashSet<>();

    public Company() {}

    public Company(String ownerId, String name, String address, String phone, String email) {
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public Company(Long id, String ownerId, String name, String address, String phone, String email) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }

    public boolean hasMember(String userId) {
        return members.contains(userId);
    }

    public boolean hasOwner(String userId) {
        return ownerId.equals(userId);
    }

    public void addMember(String userId) {
        members.add(userId);
    }

    public void removeMember(String userId) {
        members.remove(userId);
    }
}
