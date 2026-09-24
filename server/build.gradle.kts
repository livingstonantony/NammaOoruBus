plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
}

group = "com.ell.nammaooru"
version = "1.0.0"
application {
    mainClass = "com.ell.nammaooru.ApplicationKt"
}

tasks.withType<JavaExec>().configureEach {
    systemProperty("io.ktor.development", "true")
}

dependencies {
    api(project(":core"))
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.ktor.callLogging)
    implementation(libs.ktor.serverCconfigYaml)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverStatusPages)
    
    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikari)
    implementation(libs.mysql.connector)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}