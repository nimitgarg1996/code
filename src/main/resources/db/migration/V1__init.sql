CREATE TABLE IF NOT EXISTS greetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient VARCHAR(100) NOT NULL,
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO greetings (recipient, message) VALUES ('World', 'Hello from Spring Boot on JDK 25!');
