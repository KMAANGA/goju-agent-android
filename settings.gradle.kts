pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // `net.zetetic:sqlcipher-android` (the encrypted Room database) publishes directly
        // to Maven Central — no extra repository needed for it.
        mavenCentral()
    }
}

rootProject.name = "GojuAgent"

include(":app")

include(":core:core-common")
include(":core:core-designsystem")
include(":core:core-database")
include(":core:core-security")
include(":core:core-network")
include(":core:core-ussd")

include(":feature:feature-auth")
include(":feature:feature-home")
include(":feature:feature-transactions")
include(":feature:feature-customers")
include(":feature:feature-sync")
