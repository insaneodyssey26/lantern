plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

gradle.taskGraph.whenReady {
    allTasks
        .filter { it.name.startsWith("extract") && it.name.endsWith("Annotations") }
        .forEach { task ->
            val variant = task.name.removePrefix("extract").removeSuffix("Annotations").replaceFirstChar { c -> c.lowercaseChar() }
            val typedefs = task.project.file(
                "build/intermediates/annotations_typedef_file/$variant/${task.name}/typedefs.txt"
            )
            typedefs.parentFile?.mkdirs()
            if (!typedefs.exists()) typedefs.createNewFile()
            task.enabled = false
        }
}