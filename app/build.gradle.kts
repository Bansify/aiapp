plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// The release workflow passes -PversionCode/-PversionName so every published build
// gets a unique version; local builds fall back to the prototype defaults.
val resolvedVersionCode = (findProperty("versionCode") as? String)?.toIntOrNull() ?: 1
val resolvedVersionName = (findProperty("versionName") as? String)?.takeIf { it.isNotBlank() } ?: "1.0.0-proto"

android {
    namespace = "com.bansify.ai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bansify.ai"
        minSdk = 26
        targetSdk = 35
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName

        // Custom-scheme fallback for the Bansify Accounts OAuth callback
        // (`com.bansify.ai:/oauth/callback`). The verified HTTPS App Link
        // (https://ai.bansify.com/oauth/callback) is the production redirect.
        manifestPlaceholders["gothwadRedirectScheme"] = "com.bansify.ai"
    }

    buildTypes {
        debug {
            // Uses the default debug keystore (~/.android/debug.keystore)
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Unsigned release build; configure signingConfigs for release distribution
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

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }
}

dependencies {
    // Shared Bansify Accounts SDK (:core + :ui): the same sign-in surface, account
    // centre and encrypted token store as every other Bansify app. One account
    // signs you in on ai.bansify.com and in this app.
    implementation("com.github.Bansify.accountsapp:core:v1.0.11")
    implementation("com.github.Bansify.accountsapp:ui:v1.0.11")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Bansify Accounts OAuth (Custom Tabs) and the Bansify AI Worker client.
    implementation(libs.androidx.browser)
    implementation(libs.androidx.security.crypto)
    implementation(libs.tink.android)
    implementation(libs.okhttp)
}
