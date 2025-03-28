package com.msn.MSNcars.account;

import jakarta.persistence.*;

@Entity
public class FirmUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "firm_id")
    private Firm firm;
    private Long userId;

    public FirmUser() {}

    public FirmUser(Long id, Firm firm, Long userId) {
        this.id = id;
        this.firm = firm;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Firm getFirm() {
        return firm;
    }

    public void setFirm(Firm firm) {
        this.firm = firm;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
