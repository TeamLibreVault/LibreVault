package org.librevault.utils

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KProperty

class LazyVar<T>(private val initializer: () -> T) {
    private var _value: T? = null
    private var initialized = false
    private val lock = ReentrantLock()  // ensures thread-safety

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!initialized) {
            lock.withLock {
                if (!initialized) {  // double-checked locking
                    _value = initializer()
                    initialized = true
                }
            }
        }
        return _value!!
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        lock.withLock {
            _value = value
            initialized = true
        }
    }
}

fun <T> lazyVar(initializer: () -> T) = LazyVar(initializer)
