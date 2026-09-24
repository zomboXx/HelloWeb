package com.example.email.data;

import com.example.email.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {
    private final EntityManagerFactory factory;

    public UserRepository(EntityManagerFactory factory) {
        this.factory = factory;
    }

    public Optional<User> findByEmail(String email) {
        EntityManager manager = factory.createEntityManager();
        try {
            TypedQuery<User> query = manager.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            manager.close();
        }
    }

    public boolean insertIfAbsent(User user) {
        if (findByEmail(user.getEmail()).isPresent()) {
            return false;
        }

        EntityManager manager = factory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        try {
            transaction.begin();
            manager.persist(user);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (RuntimeException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }
            // The database constraint still decides a race between two requests.
            if (isUniqueEmailViolation(e)) {
                return false;
            }
            throw e;
        } finally {
            manager.close();
        }
    }

    private static boolean isUniqueEmailViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
