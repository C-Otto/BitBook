plugins {
    id("java-library")
    id("bitbook.java-conventions")
}

tasks.bootJar {
    enabled = false
    archiveClassifier.set("boot")
}
tasks.jar {
    enabled = true
}
