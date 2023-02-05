import de.cotto.javaconventions.plugins.JacocoPlugin

plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend:provider:bitaps"))
    implementation(project(":backend:provider:bitcoind"))
    implementation(project(":backend:provider:blockchaininfo"))
    implementation(project(":backend:provider:blockchair"))
    implementation(project(":backend:provider:blockcypher"))
    implementation(project(":backend:provider:blockstreaminfo"))
    implementation(project(":backend:provider:btccom"))
    implementation(project(":backend:provider:electrs"))
    implementation(project(":backend:provider:fullstackcash"))
    implementation(project(":backend:provider:mempoolspace"))
}

tasks.withType<JacocoPlugin.CheckForExecutionDataTask> {
    enabled = false
}
