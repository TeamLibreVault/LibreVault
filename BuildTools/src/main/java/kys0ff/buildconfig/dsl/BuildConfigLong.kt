package kys0ff.buildconfig.dsl

import com.android.build.api.dsl.BuildType

class BuildConfigLong(
    name: String,
    buildType: BuildType
) : BuildConfigEntry<Long>(
    buildType = buildType,
    type = "Long",
    name = name,
    format = { it.toString() + "L" }
)