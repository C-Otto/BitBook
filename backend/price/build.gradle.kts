plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":backend:models"))
    implementation(project(":backend:request"))
    implementation(project(":backend:request:models"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    integrationTestImplementation(project(":backend:models"))
}
