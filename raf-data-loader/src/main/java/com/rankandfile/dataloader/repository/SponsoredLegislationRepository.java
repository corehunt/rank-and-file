package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.SponsoredLegislation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SponsoredLegislationRepository extends JpaRepository<SponsoredLegislation, String> {

    List<SponsoredLegislation> findByPersonPersonIdAndSponsorType(String personId, String sponsorType);

    List<SponsoredLegislation> findByBillBillIdAndSponsorType(String billBillId, String sponsorType);

    @Query("SELECT sl FROM SponsoredLegislation sl JOIN FETCH sl.bill WHERE sl.person.personId = :personId AND sl.sponsorType = :sponsorType")
    Page<SponsoredLegislation> findByPerson_PersonIdAndSponsorType(String personId, String sponsorType, Pageable pageable);

    boolean existsByPerson_PersonIdAndBill_BillIdAndSponsorType(String personId, String billId, String sponsorType);

}
