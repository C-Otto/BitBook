plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation("commons-codec:commons-codec:1.15")
}

tasks.jar {
    archiveBaseName.set("backend-models")
}

pitest {
    testStrengthThreshold.set(98)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "BRANCH") {
                    limit.minimum = 0.96.toBigDecimal()
                }
            }
        }
    }
}
