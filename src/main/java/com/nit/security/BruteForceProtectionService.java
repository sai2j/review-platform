package com.nit.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final long BLOCK_DURATION_MILLISECONDS =
            15 * 60 * 1000L;

    private final Map<String, LoginAttempt> attempts =
            new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {

        LoginAttempt attempt = attempts.get(key);

        if (attempt == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();

        if (attempt.blockedUntil > currentTime) {
            return true;
        }

        if (attempt.blockedUntil != 0
                && attempt.blockedUntil <= currentTime) {

            attempts.remove(key);

            return false;
        }

        return false;
    }

    public void recordFailedAttempt(String key) {

        long currentTime = System.currentTimeMillis();

        LoginAttempt attempt =
                attempts.computeIfAbsent(
                        key,
                        value -> new LoginAttempt()
                );

        synchronized (attempt) {

            attempt.failedAttempts++;

            if (attempt.failedAttempts
                    >= MAX_FAILED_ATTEMPTS) {

                attempt.blockedUntil =
                        currentTime
                        + BLOCK_DURATION_MILLISECONDS;
            }
        }
    }

    public void recordSuccessfulLogin(String key) {

        attempts.remove(key);
    }

    private static class LoginAttempt {

        private int failedAttempts;

        private long blockedUntil;
    }
}