package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.OurMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.service.RoomService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(RoomController.class)
@Import(TestConfiguration.class)
public class RoomControllerTest {
    public static final String URL = "/room";

    @Autowired
    private OurMockMvc mvc;

    @MockBean
    private RoomService roomService;

    @Test
    public void fetchAllRooms_returnsRoomsFromService() throws Exception {
        val rooms = createRooms();
        when(roomService.getRooms()).thenReturn(rooms);

        val response = mvc.getAndMapResponse(URL, new TypeReference<List<Room>>() {
        });
        assertThat(response).isEqualTo(rooms);
    }

    private List<Room> createRooms() {
        return Arrays.asList(new Room());
    }
}
