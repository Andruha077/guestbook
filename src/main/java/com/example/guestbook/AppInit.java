
package com.example.guestbook;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInit implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Database.initialize();
        System.out.println("Guestbook application started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("Guestbook application stopped.");
    }
}
