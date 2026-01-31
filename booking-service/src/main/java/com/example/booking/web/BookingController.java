package com.example.booking.web;

import com.example.booking.model.Booking;
import com.example.booking.model.User;
import com.example.booking.repo.BookingRepository;
import com.example.booking.repo.UserRepository;
import com.example.booking.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    @PostMapping("/booking")
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@RequestBody CreateBookingRequest req, Principal principal) {
        // demo: use username from request if no principal available
        String username = principal != null ? principal.getName() : req.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // Determine roomId safely without triggering auto-unboxing of nulls
        Long roomId;
        if (req.isAutoSelect()) {
            roomId = (req.getRoomId() != null ? req.getRoomId() : Long.valueOf(1L));
        } else {
            roomId = req.getRoomId();
            if (roomId == null) {
                throw new IllegalArgumentException("roomId is required when autoSelect=false");
            }
        }
        return bookingService.createBooking(user, roomId);
    }

    @GetMapping("/bookings")
    public List<Booking> myBookings(@RequestParam(name = "username", required = false) String username, Principal principal) {
        String usern = principal != null ? principal.getName() : username;
        User user = userRepository.findByUsername(usern)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return bookingRepository.findAllByUser(user);
    }

    @Data
    public static class CreateBookingRequest {
        private String username; // for demo when no auth
        private Long roomId;
        private boolean autoSelect;
    }
}
