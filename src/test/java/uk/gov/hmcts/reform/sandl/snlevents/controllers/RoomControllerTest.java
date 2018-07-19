package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.common.OurMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.service.RoomService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(RoomController.class)
public class RoomControllerTest {
    public static final String URL = "/room";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    OurMockMvc mvc;

    @Before
    public void init() {
        mvc = new OurMockMvc(mockMvc, objectMapper);
    }

    @MockBean
    private RoomService roomService;

    @Test
    public void fetchAllRooms_returnsRoomsFromService() throws Exception {
        val rooms = createRooms();
        when(roomService.getRooms()).thenReturn(rooms);

        val response = mvc.getAndMapResponse(URL, new TypeReference<List<Room>>(){});
        assertThat(response).isEqualTo(rooms);
    }

    private List<Room> createRooms() {
        return Arrays.asList(new Room());
    }
}
