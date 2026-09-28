package com.example.email.data;

import com.example.email.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

@WebListener
public class JpaLifecycle implements ServletContextListener {
    private static final String FACTORY_KEY = "entityManagerFactory";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("email-list-mail");
        try {
            seedSampleUsers(factory);
            event.getServletContext().setAttribute(FACTORY_KEY, factory);
        } catch (RuntimeException e) {
            factory.close();
            throw e;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Object value = event.getServletContext().getAttribute(FACTORY_KEY);
        if (value instanceof EntityManagerFactory factory) {
            factory.close();
        }
        ClassLoader webAppLoader = getClass().getClassLoader();
        for (Driver driver : Collections.list(DriverManager.getDrivers())) {
            if (driver.getClass().getClassLoader() == webAppLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    event.getServletContext().log("Cannot unregister JDBC driver", e);
                }
            }
        }
    }

    public static EntityManagerFactory from(ServletContext context) {
        return (EntityManagerFactory) context.getAttribute(FACTORY_KEY);
    }

    private static void seedSampleUsers(EntityManagerFactory factory) {
        EntityManager manager = factory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        try {
            Long count = manager.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
            if (count > 0) {
                return;
            }

            transaction.begin();
            manager.persist(new User("jsmith@gmail.com", "John", "Smith"));
            manager.persist(new User("andi@murach.com", "Andrea", "Steelman"));
            manager.persist(new User("joel.murach@yahoo.com", "Joel", "Murach"));
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            manager.close();
        }
    }
}
