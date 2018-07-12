package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RoomServiceTests {

    @TestConfiguration
    static class Configuration {
        @Bean
        public RoomService createSut() {
            return new RoomService();
        }
    }

    @Autowired
    RoomService roomService;

    @MockBean
    RoomRepository roomRepository;

    @Test
    public void getRooms_returnsRoomsFromRepository() {
        val repositoryRooms = createRooms();
        when(roomRepository.findAll()).thenReturn(repositoryRooms);

        val serviceRooms = roomService.getRooms();
        assertThat(serviceRooms).isEqualTo(repositoryRooms);
    }

    private List<Room> createRooms() {
        return new ArrayList<>(Arrays.asList(new Room()));
    }
}
