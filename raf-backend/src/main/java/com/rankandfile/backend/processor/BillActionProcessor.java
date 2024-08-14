package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Action;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.util.IdGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class BillActionProcessor {

    private final IdGenerator idGenerator;

    public BillActionProcessor(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }


    public List<Action> processActionList(String json, Bill bill){
        JsonObject responseObject = JsonParser.parseString(json).getAsJsonObject();

        JsonArray billActionArray = responseObject.get("actions").getAsJsonArray();

        List<Action> actions = new ArrayList<>();

        for(int i = 0; i < billActionArray.size(); i++){
            JsonObject actionObject = billActionArray.get(i).getAsJsonObject();
            Action action = extractActionFromJsonList(actionObject);
            action.setBill(bill);
            actions.add(action);
        }

        return actions;
    }

    private Action extractActionFromJsonList(JsonObject actionObject){
        Action action = new Action();

        action.setActionId(idGenerator.generateActionId());

        action.setActionCode(actionObject.has("actionCode") && !actionObject.get("actionCode").isJsonNull() ?
                actionObject.get("actionCode").getAsString() : null);

        String latestActionDate = actionObject.has("actionDate") && !actionObject.get("actionDate").isJsonNull() ?
                actionObject.get("actionDate").getAsString() : null;
        LocalDate actionDate = latestActionDate != null ? LocalDate.parse(latestActionDate) : null;
        action.setActionDate(actionDate);

        //TODO: implement committee logic into its own table when applicable
//        if (actionObject.has("committees") && !actionObject.get("committees").isJsonNull()) {
//            JsonObject committees = actionObject.getAsJsonObject("committees");
//
//            String committeeName = (committees.has("name") && !committees.get("name").isJsonNull()) ?
//                    committees.get("name").getAsString() : null;
//            action.setCommitteeName(committeeName);
//
//            String committeeSystemCode = (committees.has("systemCode") && !committees.get("systemCode").isJsonNull()) ?
//                    committees.get("systemCode").getAsString() : null;
//            action.setCommitteeSystemCode(committeeSystemCode);
//        }


        if (actionObject.has("sourceSystem") && !actionObject.get("sourceSystem").isJsonNull()) {
            JsonObject sourceSystem = actionObject.getAsJsonObject("sourceSystem");

            Integer srcActionCode = (sourceSystem.has("code") && !sourceSystem.get("code").isJsonNull()) ?
                    sourceSystem.get("code").getAsInt() : null;
            action.setSourceSystemCode(srcActionCode);

            String srcSystemNm = (sourceSystem.has("name") && !sourceSystem.get("name").isJsonNull()) ?
                    sourceSystem.get("name").getAsString() : null;
            action.setSourceSystemName(srcSystemNm);
        }

        action.setActionText(actionObject.has("text") && !actionObject.get("text").isJsonNull() ?
                actionObject.get("text").getAsString() : null);

        action.setActionType(actionObject.has("type") && !actionObject.get("type").isJsonNull() ?
                actionObject.get("type").getAsString() : null);

//        if(actionObject.has("recordedVotes") && !actionObject.get("recordedVotes").isJsonNull()) {
//            action.setSourceSystemCode(actionObject.has("code") && !actionObject.get("code").isJsonNull() ?
//                    actionObject.get("code").getAsInt() : null);
//
//            action.setSourceSystemName(actionObject.has("name") && !actionObject.get("name").isJsonNull() ?
//                    actionObject.get("name").getAsString() : null);
//        }

        return action;
    }
}
