import org.gradle.api.tasks.testing.Test
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
    checkstyle
    id("com.github.spotbugs") version "6.4.8"
}

group = "ru.moskalev"
version = "1.0.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

ext {
    set("tomcat.version", "11.0.25")
    set("jackson-2-bom.version", "2.21.7")
    set("jackson-bom.version", "3.1.7")

    set("mapstructVersion", "1.6.3")
    set("springdocVersion", "2.8.5")
    set("bouncyCastleVersion", "1.85")
    set("opencsvVersion", "5.7.1")
    set("jspecifyVersion", "1.0.0")
    set("testcontainersVersion", "1.21.4")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.1")
    }
}

checkstyle {
    toolVersion = "10.24.0"
}

spotbugs {
    toolVersion = "4.9.8"
    ignoreFailures = false
    showStackTraces = true
}

jacoco {
    toolVersion = "0.8.11"
}

configurations.configureEach {
    resolutionStrategy {
        force(
            "org.postgresql:postgresql:42.7.12",
            "commons-beanutils:commons-beanutils:1.11.0",
            "org.xerial.snappy:snappy-java:1.1.10.9",
            "at.yawk.lz4:lz4-java:1.11.4",

            "org.apache.tomcat.embed:tomcat-embed-core:11.0.25",
            "org.apache.tomcat.embed:tomcat-embed-el:11.0.25",
            "org.apache.tomcat.embed:tomcat-embed-websocket:11.0.25",

            "com.fasterxml.jackson.core:jackson-databind:2.21.7",
            "com.fasterxml.jackson.core:jackson-core:2.21.7",

            "tools.jackson.core:jackson-databind:3.1.7",
            "tools.jackson.core:jackson-core:3.1.7"
        )
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")
    implementation("org.bouncycastle:bcprov-jdk18on:${property("bouncyCastleVersion")}")
    implementation("com.opencsv:opencsv:${property("opencsvVersion")}")
    implementation("org.jspecify:jspecify:${property("jspecifyVersion")}")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:junit-jupiter:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:postgresql:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:kafka:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:mongodb:${property("testcontainersVersion")}")

    testImplementation("org.springframework.security:spring-security-test")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(
            classDirectories.files.map { directory ->
                fileTree(directory) {
                    exclude(
                        "**/*HotelReservationApplication.class",
                        "**/config/**",
                        "**/dto/**",
                        "**/exception/**",
                        "**/enumeration/**",
                        "**/domain/**",
                        "**/integration/api/**",
                        "**/repo/**",
                        "**/specification/**",
                        "**/interceptor/**"
                    )
                }
            }
        )
    )

    doLast {
        val reportFile = reports.html.outputLocation.get().file("index.html").asFile

        if (reportFile.exists()) {
            val reportUrl = "file:///" +
                    reportFile.absolutePath.replace("\\", "/")

            println("Откройте отчет в браузере: $reportUrl")
        }
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80.toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.withType<SpotBugsTask>().configureEach {
    ignoreFailures = false
}