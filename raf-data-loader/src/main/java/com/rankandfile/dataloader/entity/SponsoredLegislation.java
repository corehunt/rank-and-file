package com.rankandfile.dataloader.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Entity
@Table(name = "raf_spons_legislation")
@Data
public class SponsoredLegislation extends RAFAudit {

    @Id
    @Column(name = "spon_leg_id")
    private String sponLegId;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    @JsonBackReference("person-sponsorship")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Person person;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    @JsonManagedReference("sponsorship-bill")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"sponsorships"})
    private Bill bill;

    @Column(name = "sponsor_type")
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
