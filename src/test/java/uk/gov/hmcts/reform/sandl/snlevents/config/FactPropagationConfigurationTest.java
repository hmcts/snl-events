package uk.gov.hmcts.reform.sandl.snlevents.config;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(SpringRunner.class)
public class FactPropagationConfigurationTest {
    private final String GOOD_TYPE = "good-type";

    @Test
    public void getMsgUrlsForMsgType_returns_correct_filtered_results(){
        FactPropagationConfiguration conf = createConfig();

        val res = conf.getMsgUrlsForMsgType(GOOD_TYPE);
    }


    @Test
    public void getMsgUrlsForMsgType_returns_nothing_when_no_type(){
        FactPropagationConfiguration conf = createConfig();

        val res = conf.getMsgUrlsForMsgType("doesNotExist");
    }

    private FactPropagationConfiguration createConfig() {
        val conf = new FactPropagationConfiguration();
        conf.setEngines(new ArrayList<>());

        val e1 = new FactPropagationEngineConfiguration();
        e1.setName("Aspen");
        e1.setMsgTypes(Arrays.asList("something-ok", GOOD_TYPE, "somethingelse", "and-much-more"));
        conf.getEngines().add(e1);

        val e2 = new FactPropagationEngineConfiguration();
        e2.setName("Lime");
        e2.setMsgTypes(Arrays.asList("tree", "leaf"));
        conf.getEngines().add(e2);

        return conf;
    }
}
