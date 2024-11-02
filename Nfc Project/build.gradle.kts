plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

tasks.register("checkDependencies") {
    doLast {
        configurations.forEach { configuration ->
            if (configuration.isCanBeResolved) {
                println("Configuration: ${configuration.name}")
                try {
                    configuration.resolvedConfiguration.lenientConfiguration.firstLevelModuleDependencies.forEach { dep ->
                        println("   ${dep.module.id.group}:${dep.module.id.name}:${dep.module.id.version}")
                    }
                } catch (e: Exception) {
                    println("   Failed to resolve: ${e.message}")
                }
            }
        }
    }
}
