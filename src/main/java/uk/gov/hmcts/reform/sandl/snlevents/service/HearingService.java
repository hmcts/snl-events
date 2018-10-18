package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.HearingSpecificationBuilder;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.SearchCriteria;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;

import java.util.List;
import javax.persistence.EntityManager;

@Service
public class HearingService {

    @Autowired
    HearingRepository hearingRepository;

    @Autowired
    private EntityManager entityManager;

    public Iterable<HearingInfo> search(List<SearchCriteria> searchCriteriaList, Pageable pegable) {

        Specification<Hearing> specification = new HearingSpecificationBuilder(entityManager).of(searchCriteriaList).build();

        return hearingRepository.findAll(specification, pegable)
            .map(HearingInfo::new);
    }
}
