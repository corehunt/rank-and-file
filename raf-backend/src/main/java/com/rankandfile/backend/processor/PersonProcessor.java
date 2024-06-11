package com.rankandfile.backend.processor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.util.IdGenerator;
import org.springframework.stereotype.Component;

@Component
public class PersonProcessor {

    private final IdGenerator idGenerator;

    public PersonProcessor(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Person validatePerson(String json) {
        JsonObject memberObject = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("member");

        Person person = new Person();
        person.setPersonId(idGenerator.generatePersonId(memberObject.get("bioguideId").getAsString()));

        person.setFirstName(memberObject.get("firstName").getAsString());
        person.setLastName(memberObject.get("lastName").getAsString());

        return person;

    }



}
