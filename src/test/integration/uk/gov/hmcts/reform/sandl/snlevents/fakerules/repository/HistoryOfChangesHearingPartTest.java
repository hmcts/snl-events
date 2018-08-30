package uk.gov.hmcts.reform.sandl.snlevents.fakerules.repository;

import lombok.val;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;

import java.time.Duration;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Java6Assertions.assertThat;

@Transactional
public class HistoryOfChangesHearingPartTest extends BaseIntegrationTestWithFakeRules {
    @Autowired
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager platformTransactionManager;

    @Test
    public void insert_shouldCreateHistory_forTheHearingPart() {
        val hearingPart = createSampleHearingPart();
        TransactionStatus newStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        hearingPartRepository.save(hearingPart);
//        entityManager.flush();

        platformTransactionManager.commit(newStatus);

        JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
        HearingPartHistoryRepository hearingPartHistoryRepository = factory.getRepository(HearingPartHistoryRepository.class);

        assertThat(hearingPartHistoryRepository.existsByEntityId_RevtypeEquals(hearingPart.getId(), (short) 0)).isTrue();
    }

    @Test
    public void update_shouldCreateHistory_forTheHearingPart() {
        val hearingPart = createSampleHearingPart();
        hearingPartRepository.save(hearingPart);
        entityManager.flush();

        hearingPart.setCaseNumber("secondchange");
        hearingPartRepository.save(hearingPart);
        entityManager.flush();

        JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
        HearingPartHistoryRepository hearingPartHistoryRepository = factory.getRepository(HearingPartHistoryRepository.class);

        // Both need to exist
        assertThat(hearingPartHistoryRepository.existsByEntityId_RevtypeEquals(hearingPart.getId(), (short) 0)).isTrue();
        assertThat(hearingPartHistoryRepository.existsByEntityId_RevtypeEquals(hearingPart.getId(), (short) 1)).isTrue();
    }

    private HearingPart createSampleHearingPart() {
        val hearingPart = new HearingPart();

        hearingPart.setId(UUID.randomUUID());
        hearingPart.setCaseNumber("AAA");
        hearingPart.setCaseTitle("tttt");
        hearingPart.setCaseType("SCLAIMS");
        hearingPart.setDuration(Duration.ofMinutes(20));
        hearingPart.setPriority(Priority.Low);

        return hearingPart;
    }

    @Repository
    public interface HearingPartHistoryRepository extends JpaRepository<HearingPart, UUID> {
        @Query(nativeQuery = true,
            value = "select case when exists(select * from hearing_part_aud where id=?1 and revtype=?2) then true else false end")
        boolean existsByEntityId_RevtypeEquals(UUID id, short revType);
    }
}
