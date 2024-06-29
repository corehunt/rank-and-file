package com.rankandfile.backend.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Data
@Component
public class IdGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generatePersonId(String bioguideId){
        String randomString = String.valueOf(random.nextInt(99999));
        return bioguideId.concat(randomString);
    }

    public Integer generateCongressId() {
        return random.nextInt(999999);
    }

    public Integer generateSessionId() {
        return random.nextInt(999999);
    }
}
