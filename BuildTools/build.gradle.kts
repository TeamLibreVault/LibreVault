plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.13.1")
}

gradlePlugin {
    plugins {
        create("buildToolsDsl") {
            id = "kys0ff.build.tools"
            implementationClass = "kys0ff.buildconfig.dsl.BuildToolsDslPlugin"
        }
    }
}