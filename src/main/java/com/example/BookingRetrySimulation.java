package com.example;

import com.example.model.RoomBooking;
import com.example.service.BookingService;
import com.example.service.BookingServiceJpa;
import com.example.util.JpaBootstrap;

import java.time.LocalDateTime;

/** Étape 9 : même conflit, mais résolu automatiquement par retry. */
public class BookingRetrySimulation {

    public static void main(String[] args) {
        BookingService service = new BookingServiceJpa();
        BookingRetryExecutor retry = new BookingRetryExecutor(service);
        try {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            RoomBooking created = service.create(new RoomBooking("Salle A", start, start.plusHours(2)));
            System.out.println("Création : " + created);

            RoomBooking user1 = service.findById(created.getId());
            RoomBooking user2 = service.findById(created.getId());

            user1.setRoomName("Salle B");
            service.update(user1);
            System.out.println("Transaction 1 : modification réussie.");

            user2.setRoomName("Salle C");
            try {
                RoomBooking result = retry.updateWithRetry(user2, b -> b.setRoomName("Salle C"));
                System.out.println("Transaction 2 : modification réussie après retry. -> " + result);
            } catch (Exception e) {
                System.err.println("Transaction 2 : échec après retries - " + e.getMessage());
            }

            System.out.println("État final en base : " + service.findById(created.getId()));
        } finally {
            JpaBootstrap.shutdown();
        }
    }
}
