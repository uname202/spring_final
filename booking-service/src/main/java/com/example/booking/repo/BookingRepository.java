package com.example.booking.repo;

import com.example.booking.model.Booking;
import com.example.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUser(User user);
}
