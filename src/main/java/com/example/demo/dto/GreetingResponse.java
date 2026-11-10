package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Greeting response payload")
public class GreetingResponse {

    @Schema(description = "Recipient name", example = "World")
    private String recipient;

    @Schema(description = "Generated greeting message", example = "Hello, World!")
    private String message;

    @Schema(description = "Timestamp when the greeting was generated")
    private Instant timestamp;
}
