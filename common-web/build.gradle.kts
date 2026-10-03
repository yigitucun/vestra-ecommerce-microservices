plugins {
    id("java")
}

group = "com.vestra"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-webmvc:7.1.0-M1")
}

tasks.test {
    useJUnitPlatform()
}