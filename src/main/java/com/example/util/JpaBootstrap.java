package com.example.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Point d'accès unique à l'EntityManagerFactory.
 * Choix de la base avec -Ddb=memory (défaut) ou -Ddb=server (H2 serveur TCP).
 */
public final class JpaBootstrap {

    private static EntityManagerFactory emf;

    private JpaBootstrap() {}

    public static synchronized EntityManager createEntityManager() {
        if (emf == null) {
            String db = System.getProperty("db", "memory").trim().toLowerCase();
            String unit = "server".equals(db) ? "bookingH2ServerPU" : "bookingH2PU";
            System.out.println(">>> Base utilisée : " + db + " (persistence unit : " + unit + ")");
            emf = Persistence.createEntityManagerFactory(unit);
        }
        return emf.createEntityManager();
    }

    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
