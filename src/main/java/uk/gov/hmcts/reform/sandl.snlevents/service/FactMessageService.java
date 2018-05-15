package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Problem;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ProblemRepository;

import java.io.IOException;

@Service
public class FactMessageService {
    private final ObjectMapper objectMapper;

    @Autowired
    private ProblemRepository problemRepository;

    public FactMessageService() {
        objectMapper = new ObjectMapper();
    }

    public void handle(String factMsg) throws IOException {
        JsonNode modifications = objectMapper.readTree(factMsg);

        for (JsonNode item : modifications) {
            if (item.get("type").asText().equals("Problem")) {
                handleProblem(item);
            }
        }
    }

    private void handleProblem(JsonNode item) {
        Problem problem = new Problem();

        JsonNode newFact = item.get("newFact");
        JsonNode oldFact = item.get("oldFact");

        if (oldFact != null && !oldFact.isNull()) {
            String id = oldFact.get("id").asText();
            if (problemRepository.exists(id)) {
                problemRepository.delete(id);
            }
        }

        if (newFact != null && !newFact.isNull()) {
            problem.setId(newFact.get("id").asText());
            problem.setMessage(newFact.get("message").asText());

            problemRepository.save(problem);
        }
    }
}
