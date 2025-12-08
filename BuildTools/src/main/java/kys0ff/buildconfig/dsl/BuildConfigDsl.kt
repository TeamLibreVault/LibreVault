package kys0ff.buildconfig.dsl

import com.android.build.api.dsl.BuildType

// Constructor functions
fun BuildType.boolean(name: String) = BuildConfigBoolean(name, this)
fun BuildType.long(name: String) = BuildConfigLong(name, this)
fun BuildType.string(name: String) = BuildConfigString(name, this)