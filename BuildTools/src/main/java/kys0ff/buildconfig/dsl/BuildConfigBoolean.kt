package kys0ff.buildconfig.dsl

import com.android.build.api.dsl.BuildType

class BuildConfigBoolean(
    name: String,
    buildType: BuildType
) : BuildConfigEntry<Boolean>(
    buildType = buildType,
    type = "Boolean",
    name = name,
    format = { it.toString() }
)