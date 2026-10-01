import com.android.build.api.artifact.SingleArtifact
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release signing: from keystore.properties (local, git-ignored) or environment variables (CI).
// Without either, assembleRelease produces an unsigned APK.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(key: String, env: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(env)

val releaseStoreFile = signingValue("storeFile", "KRANTENWIJK_KEYSTORE_FILE")

android {
    namespace = "nl.ericmulder.krantenwijk"
    compileSdk = 37

    defaultConfig {
        applicationId = "nl.ericmulder.krantenwijk"
        // Newest Android minus 5 (DEC-027): Android 12 = API 31.
        minSdk = 31
        targetSdk = 37
        versionCode = 13
        versionName = "0.12.0"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = signingValue("storePassword", "KRANTENWIJK_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "KRANTENWIJK_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "KRANTENWIJK_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        error += listOf("MissingTranslation", "ExtraTranslation", "HardcodedText")
    }

    testOptions {
        // Robolectric screen tests (DEC-021) need the merged resources.
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.useJUnitPlatform()
            // Robolectric on SDK 37 needs this JDK-internal package (robolectric/robolectric#11434).
            it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Compose screen tests on the JVM with Robolectric (DEC-021). They are JUnit 4 tests in
    // src/testDebug (the test activity comes from ui-test-manifest, a debug-only dependency) and
    // run next to the JUnit Jupiter tests through the Vintage engine.
    testDebugImplementation(platform(libs.compose.bom))
    testDebugImplementation(libs.compose.ui.test.junit4)
    testDebugImplementation(libs.robolectric)
    testDebugImplementation(libs.junit4)
    // Newer than what ui-test-junit4 pulls in; the older Espresso calls APIs removed in SDK 37.
    testDebugImplementation(libs.androidx.test.core)
    testDebugImplementation(libs.espresso.core)
    testRuntimeOnly(libs.junit.vintage.engine)
    debugImplementation(libs.compose.ui.test.manifest)
}

// DEC-003 / DEC-024: the app requests only these permissions; INTERNET exists solely for
// "Check for updates". Any other permission, including one added by a library, fails the build.
val allowedPermissions = setOf(
    "android.permission.INTERNET",
    "android.permission.REQUEST_INSTALL_PACKAGES",
    "android.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION",
    "nl.ericmulder.krantenwijk.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION", // AndroidX internal, signature-only
    "nl.ericmulder.krantenwijk.debug.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
)

abstract class VerifyPermissions : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mergedManifest: RegularFileProperty

    @get:Input
    abstract val allowed: SetProperty<String>

    @TaskAction
    fun verify() {
        val requested = Regex("""<uses-permission[^>]*android:name="([^"]+)"""")
            .findAll(mergedManifest.get().asFile.readText()).map { it.groupValues[1] }.toSet()
        val unexpected = requested - allowed.get()
        if (unexpected.isNotEmpty()) {
            throw GradleException(
                "Merged manifest requests permissions outside the allowlist (DEC-003/DEC-024): $unexpected. " +
                    "Remove them (e.g. tools:node=\"remove\") or update the allowlist with a recorded decision.",
            )
        }
    }
}

androidComponents {
    onVariants { variant ->
        val suffix = variant.name.replaceFirstChar { it.uppercase() }
        val verify = tasks.register<VerifyPermissions>("verify${suffix}Permissions") {
            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            allowed.set(allowedPermissions)
        }
        tasks.matching { it.name == "assemble$suffix" || it.name == "check" }.configureEach { dependsOn(verify) }
    }
}
