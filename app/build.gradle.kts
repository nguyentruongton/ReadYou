import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.room)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

fun fetchGitCommitHash(): String {
    return runCatching {
        val process =
            ProcessBuilder("git", "rev-parse", "--verify", "--short", "HEAD")
                .apply { environment()["GIT_CONFIG_GLOBAL"] = "/dev/null" }
                .redirectErrorStream(true)
                .start()
        process.inputStream.bufferedReader().use { it.readText().trim() }
    }.getOrDefault("dev")
}

val gitCommitHash = fetchGitCommitHash()
val keyProps = Properties()
val releaseKeyPropsFile: File = rootProject.file("signature/keystore_release.properties")
val debugKeyPropsFile: File = rootProject.file("signature/keystore.properties")

if (releaseKeyPropsFile.exists()) {
    println("Loading keystore properties from ${releaseKeyPropsFile.absolutePath}")
    keyProps.load(FileInputStream(releaseKeyPropsFile))
} else if (debugKeyPropsFile.exists()) {
    keyProps.load(FileInputStream(debugKeyPropsFile))
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.room.paging)
            implementation(libs.sqlite.bundled)
            implementation(libs.datastore.preferences.core)
            implementation(libs.okio)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okhttp)
            implementation(libs.okhttp.coroutines)
            implementation(libs.retrofit)
            implementation(libs.retrofit.gson)
            implementation(libs.paging.common.ktx)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp)
            implementation(libs.coil.svg)
            implementation(libs.opml.parser)
            implementation(libs.readability4j)
            implementation(libs.rome)
            implementation(libs.rome.modules)
        }

        commonTest.dependencies {
            implementation(libs.junit)
        }

        androidMain.dependencies {
            implementation(libs.compose.material3)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.workmanager)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose)
            implementation(libs.compose.html)
            implementation(project.dependencies.platform(libs.compose.bom.alpha))
            implementation(libs.androidx.ui.graphics)
            implementation(libs.compose.animation.graphics)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.util)
            implementation(libs.compose.material)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui.tooling)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.coil.gif)
            implementation(libs.android.svg)
            implementation(libs.telephoto)
            implementation(libs.swipe)
            implementation(libs.profileinstaller)
            implementation(libs.work.runtime.ktx)
            implementation(libs.datastore.preferences)
            implementation(libs.room.paging)
            implementation(libs.room.ktx)
            implementation(libs.paging.runtime.ktx)
            implementation(libs.paging.compose)
            implementation(libs.browser)
            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.ktx)
            implementation(libs.core.ktx)
            implementation(libs.activity.compose)
            implementation(libs.appcompat)
            implementation(libs.glance.appwidget)
            implementation(libs.glance.appwidget.preview)
            implementation(libs.glance.material3)
            implementation(libs.glance.preview)
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.navigationevent)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.timber)
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockito.core)
            implementation(libs.mockito.junit.jupiter)
            implementation(libs.mockito.kotlin)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.junit.ext)
            implementation(libs.espresso)
            implementation(libs.compose.ui.test.junit4)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.compose.material3:material3-desktop:1.12.0-alpha03")
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    add("kspAndroid", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
}

compose.desktop {
    application {
        mainClass = "me.ash.reader.MainKt"
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "me.ash.reader"
    generateResClass = always
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "me.ash.reader"
        minSdk = 26
        targetSdk = 34
        versionCode = 47
        versionName = "0.16.2"

        buildConfigField(
            "String",
            "USER_AGENT_STRING",
            "\"ReadYou/${versionName}(${versionCode})\"",
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        ksp { arg("room.incremental", "true") }
    }

    flavorDimensions.add("channel")
    productFlavors {
        create("github") {
            isDefault = true
            dimension = "channel"
        }
        create("fdroid") { dimension = "channel" }
        create("googlePlay") {
            dimension = "channel"
            applicationIdSuffix = ".google.play"
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = keyProps["keyAlias"] as String?
            keyPassword = keyProps["keyPassword"] as String?
            storeFile = keyProps["storeFile"]?.let { file(it as String) }
            storePassword = keyProps["storePassword"] as String?
        }
    }
    lint { disable.addAll(listOf("MissingTranslation", "ExtraTranslation")) }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
        all { signingConfig = signingConfigs.getByName("release") }
    }
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "ReadYou-${defaultConfig.versionName}-${gitCommitHash}.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { buildConfig = true }
    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        resources.excludes.add("rome-utils-*.jar")
    }
    androidResources { generateLocaleConfig = true }
    composeCompiler { featureFlags = setOf(ComposeFeatureFlag.PausableComposition) }
    namespace = "me.ash.reader"
}

aboutLibraries { excludeFields = arrayOf("generated") }
