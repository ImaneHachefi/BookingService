package com.example;

import com.example.model.RoomBooking;
import com.example.service.BookingService;

import javax.persistence.OptimisticLockException;
import java.util.function.Consumer;

/**
 * Stratégie de retry : en cas de conflit, on recharge la dernière version
 * en base puis on RÉAPPLIQUE la modification voulue avant de réessayer.
 */
public class BookingRetryExecutor {

    private static final int MAX_ATTEMPTS = 3;
    private final BookingService service;

    public BookingRetryExecutor(BookingService service) {
        this.service = service;
    }

    /**
     * @param firstTry copie (éventuellement obsolète) déjà modifiée, tentée en premier
     * @param change   modification métier à réappliquer sur l'état rechargé
     */
    public RoomBooking updateWithRetry(RoomBooking firstTry, Consumer<RoomBooking> change) {
        RoomBooking candidate = firstTry;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return service.update(candidate);
            } catch (OptimisticLockException e) {
                System.out.println("Conflit détecté (tentative " + attempt + "/" + MAX_ATTEMPTS + ")");
                if (attempt == MAX_ATTEMPTS) {
                    throw new OptimisticLockException("Échec après " + MAX_ATTEMPTS + " tentatives", e);
                }
                candidate = service.findById(firstTry.getId());   // dernière version en base
                change.accept(candidate);                          // on réapplique le changement
            }
        }
        throw new IllegalStateException("Inatteignable");
    }
}
