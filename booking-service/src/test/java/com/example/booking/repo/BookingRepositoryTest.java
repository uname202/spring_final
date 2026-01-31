package com.example.booking.repo;

import com.example.booking.model.Booking;
import com.example.booking.model.BookingStatus;
import com.example.booking.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByUser_returnsOnlyUsersBookings() {
        User alice = userRepository.save(User.builder().username("alice").password("p").role("USER").build());
        User bob = userRepository.save(User.builder().username("bob").password("p").role("USER").build());

        bookingRepository.save(Booking.builder()
                .user(alice).roomId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build());
        bookingRepository.save(Booking.builder()
                .user(bob).roomId(2L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build());

        List<Booking> alices = bookingRepository.findAllByUser(alice);
        assertThat(alices).hasSize(1);
        assertThat(alices.get(0).getUser().getUsername()).isEqualTo("alice");
    }
}
