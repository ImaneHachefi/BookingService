package com.example.service;

import com.example.model.RoomBooking;
import com.example.util.JpaBootstrap;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;
import java.util.List;

public class BookingServiceJpa implements BookingService {

    @Override
    public RoomBooking create(RoomBooking booking) {
        EntityManager em = JpaBootstrap.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(booking);
            tx.commit();
            return booking;
        } catch (RuntimeException e) {
            rollbackQuietly(tx);
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Met à jour une réservation DÉTACHÉE (chargée plus tôt, puis modifiée).
     *
     * On utilise merge() : Hibernate compare la version de l'objet détaché avec
     * celle de la base. Si elles diffèrent, une OptimisticLockException est levée.
     * (Un simple find() + copie des champs récupérerait la version courante et
     * masquerait le conflit.)
     */
    @Override
    public RoomBooking update(RoomBooking booking) {
        EntityManager em = JpaBootstrap.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RoomBooking merged = em.merge(booking);
            tx.commit();
            return merged;
        } catch (OptimisticLockException e) {
            rollbackQuietly(tx);
            throw e;
        } catch (RuntimeException e) {
            rollbackQuietly(tx);
            if (e instanceof RollbackException && e.getCause() instanceof OptimisticLockException) {
                throw (OptimisticLockException) e.getCause();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public RoomBooking findById(Long id) {
        EntityManager em = JpaBootstrap.createEntityManager();
        try {
            return em.find(RoomBooking.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<RoomBooking> findAll() {
        EntityManager em = JpaBootstrap.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM RoomBooking b", RoomBooking.class).getResultList();
        } finally {
            em.close();
        }
    }

    private void rollbackQuietly(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
