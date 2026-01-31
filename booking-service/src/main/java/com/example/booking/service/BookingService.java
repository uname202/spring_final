package com.example.booking.service;

import com.example.booking.model.Booking;
import com.example.booking.model.BookingStatus;
import com.example.booking.model.User;
import com.example.booking.repo.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RestClient restClient = RestClient.builder().baseUrl("http://localhost:8082").build();

    public Booking createBooking(User user, Long roomId) {
        // For the scaffold, we don't use dates in availability checks yet.
        // Provide sane defaults to satisfy non-null DB constraints.
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(1);

        Booking booking = Booking.builder()
                .user(user)
                .roomId(roomId)
                .startDate(start)
                .endDate(end)
                .status(BookingStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        try {
            ResponseEntity<ConfirmResponse> response = restClient.post()
                    .uri("/api/rooms/" + roomId + "/confirm-availability")
                    .retrieve()
                    .toEntity(ConfirmResponse.class);
            boolean ok = response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().ok());
            if (ok) {
                booking.setStatus(BookingStatus.CONFIRMED);
                return bookingRepository.save(booking);
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                safeRelease(roomId);
                return booking;
            }
        } catch (Exception ex) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            safeRelease(roomId);
            return booking;
        }
    }

    private void safeRelease(Long roomId) {
        try {
            restClient.post().uri("/api/rooms/" + roomId + "/release").retrieve().toBodilessEntity();
        } catch (Exception ignored) {
        }
    }

    // minimal DTO to map hotel-service confirmation response
    public record ConfirmResponse(Boolean ok, String message) {}
}
