package kg.example.legacy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetData() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/orders?size=1000"))
                .andReturn();
        JsonNode content = objectMapper.readTree(listResult.getResponse().getContentAsString()).path("content");
        if (content.isArray()) {
            for (JsonNode node : content) {
                mockMvc.perform(delete("/api/orders/" + node.path("id").asLong()));
            }
        }
    }

    @Test
    void createAndListOrders_withPagination() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"Alice","productName":"Laptop","price":100.00,"quantity":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(200.00));

        mockMvc.perform(get("/api/orders?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Alice"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchOrders_isCaseInsensitive() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"bob","productName":"Monitor","price":50.00,"quantity":1}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/search?q=BOB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("bob"));
    }

    @Test
    void expensiveOrders_filtersByPriceInDatabase() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"C","productName":"Cheap","price":10.00,"quantity":1}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"E","productName":"Premium","price":150.00,"quantity":1}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/expensive?price=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("Premium"));
    }

    @Test
    void getTotal_returns404ForMissingOrder() throws Exception {
        mockMvc.perform(get("/api/orders/9999/total"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAndDeleteOrder() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"Dana","productName":"Tablet","price":25.00,"quantity":2}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(put("/api/orders/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"Dana","productName":"Tablet Pro","price":30.00,"quantity":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(60.00));

        mockMvc.perform(get("/api/orders/" + id + "/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(id))
                .andExpect(jsonPath("$.total").value(60.00));

        mockMvc.perform(delete("/api/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("deleted"));

        mockMvc.perform(delete("/api/orders/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationRejectsInvalidPayload() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"","productName":"X","price":0,"quantity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("Validation failed");
    }
}
