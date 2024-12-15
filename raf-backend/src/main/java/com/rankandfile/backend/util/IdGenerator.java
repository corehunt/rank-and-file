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

    public String generateBillId(String congressNumber, String billType, String billNumber) {
        // Ensure billTypeCode is uppercase
        billType = billType.toUpperCase();

        // Pad Bill Type Code with zeros on the right to make 7 characters
        String paddedBillTypeCode = String.format("%-7s", billType).replace(' ', '0');

        // Pad Congress Number with leading zeros to make 3 digits
        String paddedCongressNumber = String.format("%-3s", congressNumber).replace(' ', '0');

        // Pad Bill Number with leading zeros to make 5 digits
        String paddedBillNumber = String.format("%-5s", billNumber).replace(' ', '0');

        return paddedBillTypeCode + paddedCongressNumber + paddedBillNumber;
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
