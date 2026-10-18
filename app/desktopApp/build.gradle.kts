import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.ell.nammaooru.MainKt"
        // Run the admin build with: ./gradlew :app:desktopApp:run -PappMode=admin
        (findProperty("appMode") as String?)?.let { jvmArgs += "-Dnammaooru.mode=$it" }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.ell.nammaooru"
            packageVersion = "1.0.0"
        }
    }
}