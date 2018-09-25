package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.RoomResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    RoomRepository roomRepository;

    public List<RoomResponse> getRoomResponses() {
        return getRooms().stream().map(RoomResponse::fromRoom).collect(Collectors.toList());
    }

    private List<Room> getRooms() {
        return roomRepository.findAll();
    }
}
