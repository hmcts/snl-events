package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.service.RoomService;

import java.util.List;

@RestController()
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    RoomService roomService;

    @RequestMapping(path = "", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody
    List<Room> fetchAllRooms() {
        return roomService.getRooms();
    }
}
