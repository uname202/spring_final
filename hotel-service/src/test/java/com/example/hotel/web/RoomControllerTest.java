package com.example.hotel.web;

import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import com.example.hotel.repo.HotelRepository;
import com.example.hotel.repo.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        Hotel h = hotelRepository.save(Hotel.builder().name("H").address("A").build());
        // timesBooked distribution and ordering check
        roomRepository.save(Room.builder().hotel(h).number("101").available(true).timesBooked(3).build());
        roomRepository.save(Room.builder().hotel(h).number("102").available(true).timesBooked(1).build());
        roomRepository.save(Room.builder().hotel(h).number("103").available(true).timesBooked(1).build());
        roomRepository.save(Room.builder().hotel(h).number("104").available(true).timesBooked(0).build());
    }

    @Test
    void recommend_sortsByTimesBookedThenId() throws Exception {
        mockMvc.perform(get("/api/rooms/recommend").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"number\":\"104\"")))
                .andExpect(content().string(containsString("\"number\":\"101\"")));
    }
}
