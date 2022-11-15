plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":cli:base"))
    implementation(project(":lnd"))
}

tasks.jar {
    archiveBaseName.set("lnd-cli")
}
