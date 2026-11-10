plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.4"
    id("checkstyle")
    id("jacoco")
    id("com.google.cloud.tools.jib") version "3.4.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    create("eval") {
        java {
            srcDir("src/eval/java")
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += output + compileClasspath
        }
        resources {
            srcDir("src/eval/resources")
        }
    }
}

val evalImplementation = configurations.getByName("evalImplementation") {
    extendsFrom(configurations.testImplementation.get())
}

val evalRuntimeOnly = configurations.getByName("evalRuntimeOnly") {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    // --- Tier 1: Core Framework & Ergonomics ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Lombok & MapStruct
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // --- Tier 4: API & Database Migration ---
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("com.h2database:h2")

    // --- Tier 5: Observability & Metrics ---
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // --- Tier 3: Testing & Testcontainers ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    // --- Decoupled Eval & Contract Testing Engine ---
    evalImplementation("io.rest-assured:rest-assured:5.5.2")
    evalImplementation("io.rest-assured:json-schema-validator:5.5.2")
    evalImplementation("com.atlassian.oai:swagger-request-validator-restassured:2.44.9")
    evalImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    evalImplementation("com.jayway.jsonpath:json-path")
}

// Spotless (Code Formatting)
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.27.0").aosp().reflowLongStrings()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// Checkstyle (Linting & Code Standards)
checkstyle {
    toolVersion = "10.21.4"
    isIgnoreFailures = false
    maxWarnings = 0
}

// JaCoCo (Test Coverage & Gates)
jacoco {
    toolVersion = "0.8.13"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

// --- Decoupled Eval & Contract Tasks ---
val evalTask = tasks.register<Test>("eval") {
    description = "Executes decoupled rule-based contract tests and eval scenarios."
    group = "verification"
    testClassesDirs = sourceSets["eval"].output.classesDirs
    classpath = sourceSets["eval"].runtimeClasspath
    useJUnitPlatform()

    val targetUrl = project.findProperty("evalUrl") ?: System.getenv("EVAL_TARGET_URL") ?: ""
    systemProperty("eval.target.url", targetUrl)
}

val runEvalEngine = tasks.register<JavaExec>("runEvalEngine") {
    description = "Executes the declarative YAML-based Eval Engine against a running Spring Boot instance."
    group = "verification"
    classpath = sourceSets["eval"].runtimeClasspath
    mainClass.set("com.example.demo.eval.EvalEngine")
    args = listOf(
        project.findProperty("evalUrl")?.toString() ?: System.getenv("EVAL_TARGET_URL") ?: "http://localhost:8080",
        file("evals").absolutePath
    )
}

// Jib Containerization Configuration
jib {
    from {
        image = "eclipse-temurin:25-jre-noble"
    }
    to {
        image = "spring-boot-harness:latest"
    }
    container {
        jvmFlags = listOf("-XX:+UseZGC")
        ports = listOf("8080")
    }
}

// Enforce coverage threshold as part of the master verification gate
tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
