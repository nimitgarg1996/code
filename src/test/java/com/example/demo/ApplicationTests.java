package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.controller.GreetingController;
import com.example.demo.dto.GreetingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

    @Autowired private GreetingController greetingController;

    @Test
    @DisplayName("Context loads successfully and controller is injected")
    void contextLoads() {
        assertThat(greetingController).isNotNull();
    }

    @Test
    @DisplayName("Greeting controller generates expected greeting message")
    void greetingGeneratesCorrectPayload() {
        GreetingResponse response = greetingController.greet("Antigravity");
        assertThat(response).isNotNull();
        assertThat(response.getRecipient()).isEqualTo("Antigravity");
        assertThat(response.getMessage()).isEqualTo("Hello, Antigravity!");
        assertThat(response.getTimestamp()).isNotNull();
    }
}
