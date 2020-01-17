package it.unibo.tuprolog.core.impl

import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import kotlin.jvm.Synchronized

internal class VarImpl(override val name: String, private val identifier: Int = instanceId()) : TermImpl(), Var {

    companion object {
        private var instanceCount = 0

        @Synchronized
        private fun instanceId() = instanceCount++
    }

    override val completeName: String by lazy {
        "${name}_$identifier"
    }

    override val isAnonymous: Boolean = super.isAnonymous

    override val isNameWellFormed: Boolean by lazy {
        Var.VAR_REGEX_PATTERN.matches(name)
    }

    override fun structurallyEquals(other: Term): Boolean = other is VarImpl

    override fun freshCopy(): Var = VarImpl(name)

    override fun toString(): String = if (isNameWellFormed) completeName else "¿$completeName?"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Var) return false

        if (completeName != other.completeName) return false

        return true
    }

    override fun hashCode(): Int = completeName.hashCode()
}
