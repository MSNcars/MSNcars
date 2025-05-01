package com.msn.msncars.invitation;

import com.msn.msncars.company.Company;
import jakarta.persistence.*;

import java.time.*;
import java.util.UUID;

@Entity
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String recipientUserId;
    @ManyToOne
    @JoinColumn(name = "sender_company_id")
    private Company senderCompany;
    private Instant creationDateTime;
    @Enumerated(EnumType.STRING)
    private InvitationState invitationState;
    @Transient
    private Clock clock;

    public Invitation() {
        clock = Clock.systemUTC();
    }

    public Invitation(String recipientUserId, Company senderCompany, Instant creationDateTime, InvitationState invitationState, Clock clock) {
        this.recipientUserId = recipientUserId;
        this.senderCompany = senderCompany;
        this.creationDateTime = creationDateTime;
        this.invitationState = invitationState;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @PrePersist
    public void setCreationDateTime() {
        if (creationDateTime == null)
            creationDateTime = Instant.now(clock);
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public String getFormattedDateForUser(ZoneId userTimeZone) {
        ZonedDateTime localDateTime = creationDateTime.atZone(clock.getZone()).withZoneSameInstant(userTimeZone);
        return localDateTime.toLocalDate().toString();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public Company getSenderCompany() {
        return senderCompany;
    }

    public void setSenderCompany(Company senderCompany) {
        this.senderCompany = senderCompany;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public InvitationState getInvitationState() {
        return invitationState;
    }

    public void setInvitationState(InvitationState invitationState) {
        this.invitationState = invitationState;
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void accept() {
        senderCompany.addMember(recipientUserId);
        invitationState = InvitationState.ACCEPTED;
    }

    public void decline() {
        invitationState = InvitationState.DECLINED;
    }
}