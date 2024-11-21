package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Entity
@Table(name = "RAF_SPONS_LEGISLATION")
@Data
public class SponsoredLegislation extends RAFAudit {

    @Id
    @Column(name = "SPON_LEG_ID")
    private String sponLegId;

    @ManyToOne
    @JoinColumn(name = "PERSON_ID", nullable = false)
    @JsonBackReference("person-sponsorship")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Person person;

    @ManyToOne
    @JoinColumn(name = "BILL_ID", nullable = false)
    @JsonManagedReference("sponsorship-bill")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"sponsorships"})
    private Bill bill;

    @Column(name = "SPONSOR_TYPE")
    private String sponsorType; // Values: "Sponsor" or "Co-Sponsor"

    // Override equals and hashCode based on person and bill
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SponsoredLegislation that = (SponsoredLegislation) o;

        if (!person.equals(that.person)) return false;
        return bill.equals(that.bill);
    }

    @Override
    public int hashCode() {
        int result = person.hashCode();
        result = 31 * result + bill.hashCode();
        return result;
    }
    
}
