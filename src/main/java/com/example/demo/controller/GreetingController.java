package com.example.demo.controller;

import com.example.demo.dto.GreetingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/greetings")
@Tag(name = "Greetings API", description = "Endpoints for greeting operations")
public class GreetingController {

    @GetMapping
    @Operation(summary = "Generate a personalized greeting")
    public GreetingResponse greet(
            @Parameter(description = "Name of the person to greet")
                    @RequestParam(defaultValue = "World")
                    String name) {
        return GreetingResponse.builder()
                .recipient(name)
                .message("Hello, " + name + "!")
                .timestamp(Instant.now())
                .build();
    }
}
