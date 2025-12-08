package kys0ff.buildconfig.dsl

import com.android.build.api.dsl.BuildType

abstract class BuildConfigEntry<T>(
    val buildType: BuildType,
    val type: String,
    val name: String,
    val format: (T) -> String
) {
    infix fun set(value: T) =
        buildType.buildConfigField(type, name, format(value))
}