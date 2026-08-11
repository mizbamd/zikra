import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun local(key: String, default: String = "", envName: String? = null): String =
    localProps.getProperty(key)
        ?: System.getenv(envName ?: key.replace('.', '_').uppercase())
        ?: default

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.mizbamd.zikra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mizbamd.zikra"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", buildConfigString(local("google.web.client.id")))
    }

    signingConfigs {
        create("release") {
            val raw = local("zikra.keystore.file", envName = "ZIKRA_KEYSTORE_FILE")
            if (raw.isBlank()) return@create
            val f = file(raw).let { if (it.isAbsolute) it else rootProject.file(raw) }
            if (!f.isFile) return@create
            storeFile = f
            storePassword = local("zikra.keystore.password", envName = "ZIKRA_KEYSTORE_PASSWORD")
            keyAlias = local("zikra.keystore.alias", "upload", envName = "ZIKRA_KEYSTORE_ALIAS")
            keyPassword = local("zikra.keystore.key.password", envName = "ZIKRA_KEYSTORE_KEY_PASSWORD")
                .ifBlank { storePassword }
        }
    }

    buildTypes {
        debug {
            // Emulator default. Physical device on LAN: api.base.url=http://<lan-ip>:8080
            buildConfigField(
                "String",
                "API_BASE_URL",
                buildConfigString(local("api.base.url", "http://10.0.2.2:8080")),
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Never inherit the emulator URL. Override via api.base.url.release / API_BASE_URL_RELEASE.
            buildConfigField(
                "String",
                "API_BASE_URL",
                buildConfigString(local("api.base.url.release", "https://api.zikra.app")),
            )
            ndk {
                abiFilters += listOf("armeabi-v7a", "arm64-v8a")
            }
            val rel = signingConfigs.getByName("release")
            if (rel.storeFile?.isFile == true) {
                signingConfig = rel
            }
        }
    }

    bundle {
        abi { enableSplit = true }
        density { enableSplit = true }
        language { enableSplit = true }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    sourceSets.getByName("main").assets.srcDir(rootProject.file("../catalog"))
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.9.3")
    // AppCompatDelegate.setApplicationLocales (per-app language on API 26–32).
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation(platform("io.insert-koin:koin-bom:4.0.0"))
    implementation("io.insert-koin:koin-android")
    implementation("io.insert-koin:koin-androidx-compose")

    val ktor = "2.3.13"
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-okhttp:$ktor")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
