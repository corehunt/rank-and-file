package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rankandfile.backend.entity.Congress;
import com.rankandfile.backend.entity.Session;
import com.rankandfile.backend.repository.CongressRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class CongressProcessor {

    private final CongressRepository congressRepository;

    private final IdGenerator idGenerator;

    @Autowired
    public CongressProcessor(CongressRepository congressRepository, IdGenerator idGenerator) {
        this.congressRepository = congressRepository;
        this.idGenerator = idGenerator;
    }

    public Congress processCongressData(JsonObject congressObject) {
        Integer congressNumber = congressObject.get("number").getAsInt();
        String congressName = congressObject.get("name").getAsString();
        String startYear = congressObject.get("startYear").getAsString();
        String endYear = congressObject.get("endYear").getAsString();

        Optional<Congress> optionalCongress = congressRepository.findByCongressNumber(congressNumber);
        Congress congress = optionalCongress.orElseGet(() -> {
            Congress newCongress = new Congress();
            newCongress.setCongressId(idGenerator.generateCongressId()); // Use the congress number as the ID
            newCongress.setCongressNumber(congressNumber);
            newCongress.setCongressName(congressName);
            newCongress.setStartYear(startYear);
            newCongress.setEndYear(endYear);
            return newCongress;
        });

        if (congressObject.has("sessions")) {
            JsonArray sessionsArray = congressObject.getAsJsonArray("sessions");
            Set<Session> sessions = new HashSet<>();
            for (int i = 0; i < sessionsArray.size(); i++) {
                JsonObject sessionObject = sessionsArray.get(i).getAsJsonObject();
                Session session = new Session();
                session.setSessionId(idGenerator.generateSessionId());
                session.setChamber(sessionObject.get("chamber").getAsString());
                session.setNumber(sessionObject.get("number").getAsInt());
                session.setType(sessionObject.get("type").getAsString());
                session.setStartDate(LocalDate.parse(sessionObject.get("startDate").getAsString()));
                session.setEndDate(sessionObject.has("endDate") && !sessionObject.get("endDate").isJsonNull() ? LocalDate.parse(sessionObject.get("endDate").getAsString()) : null);
                session.setCongress(congress);
                sessions.add(session);
            }
            congress.setSessions(sessions);
        }

        return congress;
    }

}
