package com.rankandfile.backend.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Data
@Component
public class IdGenerator {

    private static final String actionBillPrefix = "AB";

    private static final String sponsLegPrefix = "SL";

    private static final String committeePrefix = "CM";

    private static final String textPrefix = "TX";

    private static final SecureRandom random = new SecureRandom();

    public Integer generateCongressId() {
        return random.nextInt(999999);
    }

    public Integer generateSessionId() {
        return random.nextInt(999999);
    }

    public Integer generateTermId(){return random.nextInt(999999);}

    public String generateBillId(Integer congressId, String billType, Integer billNo){
        return congressId.toString().concat("-").concat(billType).concat("-").concat(billNo.toString());
    }

    public String generateActionId() {
        return actionBillPrefix.concat(String.valueOf(random.nextInt(999999)));
    }

    public String generateSponsLegId() {
        return sponsLegPrefix.concat(String.valueOf(random.nextInt(999999)));
    }

    public String generateCommitteeId(String systemCode) {
        return committeePrefix + systemCode;
    }

    public String generateTextId(){
        return textPrefix.concat(String.valueOf(random.nextInt(999999)));
    }
}
