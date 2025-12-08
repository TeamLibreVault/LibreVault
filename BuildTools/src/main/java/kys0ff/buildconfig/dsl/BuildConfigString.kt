package kys0ff.buildconfig.dsl

import com.android.build.api.dsl.BuildType

class BuildConfigString(
    name: String,
    buildType: BuildType
) : BuildConfigEntry<String>(
    buildType = buildType,
    type = "String",
    name = name,
    format = { "\"$it\"" }
)