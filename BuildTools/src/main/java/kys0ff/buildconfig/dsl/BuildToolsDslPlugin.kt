package kys0ff.buildconfig.dsl

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildToolsDslPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // No-op: plugin exists only to expose extension funcs
    }
}