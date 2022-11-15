plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend:models"))
    implementation(project(":backend:transaction"))
    implementation(project(":backend:price"))
    implementation(project(":ownership"))
    implementation(project(":cli:base"))
    testImplementation(testFixtures(project(":backend:transaction")))
    testImplementation(testFixtures(project(":backend:models")))
}

tasks.jar {
    archiveBaseName.set("ownership-cli")
}
