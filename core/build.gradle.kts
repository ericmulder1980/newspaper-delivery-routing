import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Domain + data layer as a Kotlin Multiplatform module (DEC-013).
// The jvm target exists only so domain and database tests run on the host with the same
// bundled SQLite as the phone; it is not shipped.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    // Room's generated KrantenwijkDatabaseConstructor is an expect/actual object (Beta in Kotlin).
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

    android {
        namespace = "nl.ericmulder.krantenwijk.core"
        compileSdk = 37
        minSdk = 26
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            // api: the app's DI module handles KrantenwijkDatabase (a RoomDatabase) and DataStore types.
            api(libs.room.runtime)
            api(libs.datastore.preferences.core)
            implementation(libs.sqlite.bundled)
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.junit.bom))
            implementation(libs.junit.jupiter)
            runtimeOnly(libs.junit.platform.launcher)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.room.testing)
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

// Kotlin Multiplatform creates no `test` task, so a plain `./gradlew test` would silently skip
// this module. This alias makes `./gradlew test` run every test in the project.
tasks.register("test") {
    group = "verification"
    description = "Runs the JVM tests of :core (alias for jvmTest)."
    dependsOn("jvmTest")
}
