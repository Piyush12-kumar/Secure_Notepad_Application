package com.example.notepadapp.Service;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Date expiryDate) {
        blacklistedTokens.put(token, expiryDate);
        cleanupExpiredTokens();
    }

    public boolean isBlacklisted(String token) {
        if(!blacklistedTokens.containsKey(token)){
            return false;
        }

        Date expiryDate = blacklistedTokens.get(token);
        if(expiryDate.before(new Date())){
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    public void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry->entry.getValue().before(now));
    }
}
