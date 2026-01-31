package com.example.hotel.web;

import com.example.hotel.model.Hotel;
import com.example.hotel.repo.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
    private final HotelRepository hotelRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel create(@RequestBody Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @GetMapping
    public List<Hotel> list() {
        return hotelRepository.findAll();
    }
}
