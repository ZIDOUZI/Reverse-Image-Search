import java.time.LocalDateTime
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val keystoreFile = rootProject.file("keystore.properties")
val keystore = Properties().apply { load(keystoreFile.reader()) }

fun getVersionCode(): Int = keystore["VERSION_CODE"].toString().toInt().apply {
    if ("assembleRelease" in gradle.startParameter.taskNames) {
        keystore["VERSION_CODE"] = plus(1).toString()
        keystore.store(keystoreFile.writer(), "last release: ${LocalDateTime.now()}")
    }
}

fun getInfo(key: String): String = keystore[key].toString()

android {
    namespace = "zdz.revimg"
    compileSdk = 35

    defaultConfig {
        applicationId = "zdz.revimg"
        minSdk = 24
        targetSdk = 35
        versionCode = getVersionCode()
        versionName = "v${getInfo("VERSION")}-${getInfo("VERSION_NAME")}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(getInfo("STORE_FILE"))
            storePassword = getInfo("STORE_PASSWORD")
            keyAlias = getInfo("DEBUG_ALIAS")
            keyPassword = getInfo("DEBUG_PASSWORD")
        }
        create("release") {
            storeFile = file(getInfo("STORE_FILE"))
            storePassword = getInfo("STORE_PASSWORD")
            keyAlias = getInfo("RELEASE_ALIAS")
            keyPassword = getInfo("RELEASE_PASSWORD")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "锟酵锟斤拷"
            buildConfigField("String", "VERSION", "\"0.0.0\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "VERSION", "\"${getInfo("VERSION")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions.jvmTarget = "11"
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures.buildConfig = true
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(project(":api"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}