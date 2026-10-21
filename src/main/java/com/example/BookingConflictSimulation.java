package com.example;

import com.example.model.RoomBooking;
import com.example.service.BookingService;
import com.example.service.BookingServiceJpa;
import com.example.util.JpaBootstrap;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

/** Étape 6 : provoque un conflit optimiste entre deux "utilisateurs". */
public class BookingConflictSimulation {

    public static void main(String[] args) {
        BookingService service = new BookingServiceJpa();
        try {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            RoomBooking created = service.create(new RoomBooking("Salle A", start, start.plusHours(2)));
            System.out.println("Création : " + created);

            // Deux utilisateurs chargent la même réservation (même version)
            RoomBooking user1 = service.findById(created.getId());
            RoomBooking user2 = service.findById(created.getId());
            System.out.println("Utilisateur 1 a chargé : " + user1);
            System.out.println("Utilisateur 2 a chargé : " + user2);

            // Utilisateur 1 modifie en premier -> OK, version passe à 1
            user1.setRoomName("Salle B");
            RoomBooking saved = service.update(user1);
            System.out.println("Transaction 1 : modification réussie. -> " + saved);

            // Utilisateur 2 travaille sur une version obsolète -> conflit
            try {
                user2.setRoomName("Salle C");
                service.update(user2);
                System.out.println("Transaction 2 : modification réussie.");
            } catch (OptimisticLockException e) {
                System.err.println("Transaction 2 : conflit détecté - " + e.getMessage());
            }

            System.out.println("État final en base : " + service.findById(created.getId()));
        } finally {
            JpaBootstrap.shutdown();
        }
    }
}
