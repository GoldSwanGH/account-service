package com.academy.accountservice.logging;

import com.academy.accountservice.data.LogRepository;
import com.academy.accountservice.data.entities.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class SecurityEventLogger {

    private static class LazySingleton {
        private static final SecurityEventLogger INSTANCE = new SecurityEventLogger();
    }

    @Autowired
    private LogRepository repo;

    private SecurityEventLogger() { }

    public static SecurityEventLogger getInstance() {
        return LazySingleton.INSTANCE;
    }

    public void log(SecurityEvent action, String subject, String object, String path) {

        LocalDate date = LocalDate.now();
        Log log = new Log();
        log.setDate(date);
        log.setAction(action);
        log.setSubject(subject);
        log.setObject(object);
        log.setPath(path);

        repo.save(log);
    }

}

