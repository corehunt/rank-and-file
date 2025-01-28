package com.rankandfile.dataloader.service.internal;

import com.rankandfile.dataloader.dto.SponsoredLegislationDTO;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import com.rankandfile.dataloader.mapper.SponsoredLegislationMapper;
import com.rankandfile.dataloader.repository.SponsoredLegislationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SponsoredLegislationService {

    private final SponsoredLegislationRepository sponsoredLegislationRepository;
    private final SponsoredLegislationMapper sponsoredLegislationMapper;

    public SponsoredLegislationService(SponsoredLegislationRepository sponsoredLegislationRepository,
                                       SponsoredLegislationMapper sponsoredLegislationMapper) {
        this.sponsoredLegislationRepository = sponsoredLegislationRepository;
        this.sponsoredLegislationMapper = sponsoredLegislationMapper;
    }

    public Page<SponsoredLegislationDTO> getSponsoredLegislation(String personId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bill.introducedDt").descending());

        Page<SponsoredLegislation> sponLegPage = sponsoredLegislationRepository.findByPerson_PersonIdAndSponsorType(
                personId, "Sponsor", pageable);

        return sponLegPage.map(sponsoredLegislationMapper::toSponsoredLegislationDTO);
    }

    public Page<SponsoredLegislationDTO> getCoSponsoredLegislation(String personId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bill.introducedDt").descending());

        Page<SponsoredLegislation> coSponLegPage = sponsoredLegislationRepository.findByPerson_PersonIdAndSponsorType(
                personId, "Co-Sponsor", pageable);

        return coSponLegPage.map(sponsoredLegislationMapper::toSponsoredLegislationDTO);
    }
}
