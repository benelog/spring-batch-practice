package kr.co.wikibook.batch.webadmin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class JobControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void startHelloJob() throws Exception {
    String runId = UUID.randomUUID().toString();
    mockMvc.perform(
        post("/jobs/{jobName}/start", "helloJob")
            .param("id", runId)
    ).andExpect(status().isOk());
  }

  @Test
  void startHello2Job() throws Exception {
    // runIdIncrementor가 지정된 잡
    mockMvc.perform(
        post("/jobs/{jobName}/start", "helloJob")
    ).andExpect(status().isOk());
  }

  @Test
  void startSlowJob() throws Exception {
    String runId = UUID.randomUUID().toString();
    mockMvc.perform(
        post("/jobs/{jobName}/start", "slowJob")
            .param("limit", "5,java.lang.Long,true")
            .param("id", runId)
    ).andExpect(status().isOk());
  }
}
