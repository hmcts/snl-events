package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

}
