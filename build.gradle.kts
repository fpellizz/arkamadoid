plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("com.android.application") version "8.2.2" apply false
    kotlin("android") version "1.9.22" apply false
}

allprojects {
    group = "com.arkamadoid"
    version = "0.1.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
