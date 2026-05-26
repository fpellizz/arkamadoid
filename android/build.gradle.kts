plugins {
    id("com.android.application")
    kotlin("android")
}

val gdxVersion: String by project

android {
    namespace = "com.arkamadoid.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.arkamadoid"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("../assets")
            java.srcDirs("src/main/kotlin")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

configurations { create("natives") }

dependencies {
    implementation(project(":core"))

    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    "natives"("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    "natives"("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    "natives"("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    "natives"("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi-v7a")
    "natives"("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-arm64-v8a")
    "natives"("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86_64")
    "natives"("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a")
    "natives"("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a")
    "natives"("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64")

    // Google Play Games Services v2
    implementation("com.google.android.gms:play-services-games-v2:20.1.2")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
}

tasks.register<Copy>("copyAndroidNatives") {
    doFirst { file("libs/armeabi-v7a/").mkdirs(); file("libs/arm64-v8a/").mkdirs(); file("libs/x86_64/").mkdirs() }
    configurations["natives"].files.forEach { jar ->
        val outputDir = when {
            jar.name.endsWith("natives-armeabi-v7a.jar") -> file("libs/armeabi-v7a")
            jar.name.endsWith("natives-arm64-v8a.jar") -> file("libs/arm64-v8a")
            jar.name.endsWith("natives-x86_64.jar") -> file("libs/x86_64")
            else -> null
        }
        outputDir?.let { from(zipTree(jar)) { into(it) } }
    }
}

tasks.matching { it.name.contains("package") || it.name.contains("assemble") }.configureEach {
    dependsOn("copyAndroidNatives")
}
