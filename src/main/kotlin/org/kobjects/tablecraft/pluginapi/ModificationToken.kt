package org.kobjects.tablecraft.pluginapi

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class ModificationToken private constructor(val tag: Long = System.nanoTime()) {

    companion object {
        private val lock = ReentrantLock()

        @OptIn(ExperimentalContracts::class)
        fun <T> applySynchronizedWithToken(action: (ModificationToken) -> T): T {
            contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
            return lock.withLock {
                val modificationToken = ModificationToken()
                action(modificationToken)
            }
        }

        fun <T> applySynchronized(action: () -> T) = lock.withLock(action)
    }
}