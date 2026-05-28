import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
}

val gdxVersion: String by project

// keystore.properties è gitignored; in CI i parametri arrivano dalle env vars.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use(::load)
}

fun keystoreValue(propKey: String, envKey: String): String? =
    keystoreProps.getProperty(propKey) ?: System.getenv(envKey)?.takeIf { it.isNotBlank() }

android {
    namespace = "com.arkamadoid.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.arkamadoid"
        minSdk = 26
        targetSdk = 34
        versionCode = (keystoreValue("versionCode", "VERSION_CODE")?.toIntOrNull()) ?: 1
        versionName = keystoreValue("versionName", "VERSION_NAME") ?: "0.1.0"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            val storePath = keystoreValue("storeFile", "RELEASE_KEYSTORE_PATH")
            val storePass = keystoreValue("storePassword", "RELEASE_KEYSTORE_PASSWORD")
            val aliasName = keystoreValue("keyAlias", "RELEASE_KEY_ALIAS")
            val keyPass = keystoreValue("keyPassword", "RELEASE_KEY_PASSWORD")
            if (storePath != null && storePass != null && aliasName != null && keyPass != null) {
                storeFile = rootProject.file(storePath)
                storePassword = storePass
                keyAlias = aliasName
                keyPassword = keyPass
            }
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("../assets")
            java.srcDirs("src/main/kotlin")
            jniLibs.srcDirs("build/libs/natives")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // applica la signing config solo se la keystore è configurata
            val sc = signingConfigs.getByName("release")
            if (sc.storeFile != null) signingConfig = sc
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

    androidResources {
        noCompress += listOf("ogg", "mp3", "ttf", "fnt")
    }
}

val natives: Configuration by configurations.creating

dependencies {
    implementation(project(":core"))

    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86_64")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64")

    // Google Play Games Services v2
    implementation("com.google.android.gms:play-services-games-v2:20.1.2")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
}

tasks.register("copyAndroidNatives") {
    val nativesDir = layout.buildDirectory.dir("libs/natives").get().asFile
    inputs.files(natives)
    outputs.dir(nativesDir)
    doLast {
        natives.files.forEach { jar ->
            val abi = when {
                jar.name.endsWith("natives-armeabi-v7a.jar") -> "armeabi-v7a"
                jar.name.endsWith("natives-arm64-v8a.jar") -> "arm64-v8a"
                jar.name.endsWith("natives-x86_64.jar") -> "x86_64"
                else -> return@forEach
            }
            copy {
                from(zipTree(jar)) { include("*.so") }
                into(nativesDir.resolve(abi))
            }
        }
    }
}

tasks.matching { it.name.startsWith("merge") && it.name.contains("JniLibFolders") }.configureEach {
    dependsOn("copyAndroidNatives")
}
tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("copyAndroidNatives")
}
