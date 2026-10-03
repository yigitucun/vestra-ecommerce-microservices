plugins {
    id("java")
}

group = "com.vestra"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
}