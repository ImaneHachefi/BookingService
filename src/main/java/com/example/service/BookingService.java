package com.example.service;

import com.example.model.RoomBooking;
import java.util.List;

public interface BookingService {
    RoomBooking create(RoomBooking booking);
    RoomBooking update(RoomBooking booking);
    RoomBooking findById(Long id);
    List<RoomBooking> findAll();
}
