package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.SponsoredLegislation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SponsoredLegislationRepository extends JpaRepository<SponsoredLegislation, String> {

    List<SponsoredLegislation> findByPersonPersonId(String personId);

    List<SponsoredLegislation> findByPersonPersonIdAndSponsorType(String personId, String sponsorType);

}
