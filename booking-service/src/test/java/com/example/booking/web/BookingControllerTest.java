package com.example.booking.web;

import com.example.booking.model.User;
import com.example.booking.repo.BookingRepository;
import com.example.booking.repo.UserRepository;
import com.example.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_returns400_whenRoomIdMissingAndAutoSelectFalse() throws Exception {
        String payload = "{" +
                "\"username\":\"alice\"," +
                "\"autoSelect\":false" +
                "}";

        Mockito.when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(User.builder().id(1L).username("alice").password("p").role("USER").build()));

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("roomId is required")));
    }

    @Test
    void createBooking_returns201_whenValidRequest() throws Exception {
        String payload = "{" +
                "\"username\":\"alice\"," +
                "\"roomId\":1," +
                "\"autoSelect\":false" +
                "}";

        Mockito.when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(User.builder().id(1L).username("alice").password("p").role("USER").build()));

        Mockito.when(bookingService.createBooking(Mockito.any(), Mockito.eq(1L)))
                .thenAnswer(inv -> com.example.booking.model.Booking.builder()
                        .id(42L)
                        .user(inv.getArgument(0))
                        .roomId(1L)
                        .status(com.example.booking.model.BookingStatus.CONFIRMED)
                        .startDate(java.time.LocalDate.now())
                        .endDate(java.time.LocalDate.now().plusDays(1))
                        .createdAt(java.time.OffsetDateTime.now())
                        .build());

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }
}
