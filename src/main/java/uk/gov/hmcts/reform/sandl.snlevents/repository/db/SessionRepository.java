package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("FROM Session WHERE start BETWEEN :dateStart AND :dateEnd")
    List<Session> findSessionByStartDate(@Param("dateStart") OffsetDateTime dateStart,
                                                @Param("dateEnd") OffsetDateTime dateEnd);
}
