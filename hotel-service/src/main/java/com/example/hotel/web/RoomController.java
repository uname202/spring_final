package com.example.hotel.web;

import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import com.example.hotel.repo.HotelRepository;
import com.example.hotel.repo.RoomRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    // naive in-memory locks for demo
    private final Map<Long, Boolean> roomLocks = new ConcurrentHashMap<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room create(@RequestBody CreateRoomRequest req) {
        Hotel hotel = hotelRepository.findById(req.getHotelId())
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found"));
        Room room = Room.builder()
                .hotel(hotel)
                .number(req.getNumber())
                .available(true)
                .timesBooked(0)
                .build();
        return roomRepository.save(room);
    }

    @GetMapping
    public List<Room> listAvailable() {
        return roomRepository.findAllByAvailableTrue();
    }

    @GetMapping("/recommend")
    public List<Room> recommend() {
        return roomRepository.findAllByAvailableTrue().stream()
                .sorted(Comparator.comparingLong(Room::getTimesBooked).thenComparing(Room::getId))
                .toList();
    }

    // INTERNAL: confirm availability and place a temporary lock + increment counter
    @PostMapping("/{id}/confirm-availability")
    public ConfirmResponse confirm(@PathVariable("id") Long id) {
        Optional<Room> opt = roomRepository.findById(id);
        if (opt.isEmpty()) return new ConfirmResponse(false, "Room not found");
        Room room = opt.get();
        if (!room.isAvailable()) {
            return new ConfirmResponse(false, "Room not available");
        }
        boolean locked = roomLocks.putIfAbsent(id, true) == null;
        if (!locked) {
            return new ConfirmResponse(false, "Room temporarily locked");
        }
        // simulate increment of timesBooked on successful confirmation
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);
        return new ConfirmResponse(true, "confirmed");
    }

    // INTERNAL: release temporary lock
    @PostMapping("/{id}/release")
    public ConfirmResponse release(@PathVariable("id") Long id) {
        roomLocks.remove(id);
        return new ConfirmResponse(true, "released");
    }

    @Data
    public static class CreateRoomRequest {
        private Long hotelId;
        private String number;
    }

    public record ConfirmResponse(boolean ok, String message) {}
}
