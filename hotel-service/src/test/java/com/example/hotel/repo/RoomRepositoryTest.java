package com.example.hotel.repo;

import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void findAllByAvailableTrue_returnsOnlyAvailableRooms() {
        Hotel h = hotelRepository.save(Hotel.builder().name("H").address("A").build());
        roomRepository.save(Room.builder().hotel(h).number("101").available(true).timesBooked(0).build());
        roomRepository.save(Room.builder().hotel(h).number("102").available(false).timesBooked(0).build());

        List<Room> available = roomRepository.findAllByAvailableTrue();
        assertThat(available).hasSize(1);
        assertThat(available.get(0).getNumber()).isEqualTo("101");
    }
}
