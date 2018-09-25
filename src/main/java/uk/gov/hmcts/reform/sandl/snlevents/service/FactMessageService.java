package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateProblem;

import java.io.IOException;
import java.util.UUID;

@Service
public class FactMessageService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemService problemService;

    public void handle(UUID userTransactionId, String factMsg) {
        try {
            JsonNode modifications = objectMapper.readTree(factMsg);

            for (JsonNode item : modifications) {
                if ("Problem".equals(item.get("type").asText())) {
                    handleProblem(userTransactionId, item);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleProblem(UUID userTransactionId, JsonNode item) throws IOException {
        JsonNode newFact = item.get("newFact");
        JsonNode oldFact = item.get("oldFact");

        if (oldFact != null && !oldFact.isNull()) {
            String id = oldFact.get("id").asText();
            problemService.removeIfExist(id);
        }

        if (newFact != null && !newFact.isNull()) {
            CreateProblem createProblem = objectMapper.readValue(newFact.traverse(), CreateProblem.class);
            Problem problem = problemService.problemCreateToDb(createProblem);
            if (userTransactionId != null) {
                problem.setUserTransactionId(userTransactionId);
            }
            problemService.save(problem);
        }
    }
}
