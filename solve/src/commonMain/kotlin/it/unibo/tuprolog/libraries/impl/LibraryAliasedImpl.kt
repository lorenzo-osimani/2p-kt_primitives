package it.unibo.tuprolog.libraries.impl

import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.libraries.LibraryAliased
import it.unibo.tuprolog.primitive.Primitive
import it.unibo.tuprolog.primitive.Signature
import it.unibo.tuprolog.theory.ClauseDatabase

/**
 * Default implementation class of [LibraryAliased]
 *
 * @author Enrico
 */
internal open class LibraryAliasedImpl(
        override val operators: OperatorSet,
        override val theory: ClauseDatabase,
        override val primitives: Map<Signature, Primitive>,
        override val alias: String
) : LibraryImpl(operators, theory, primitives), LibraryAliased{

    override fun toString(): String =
            "Library(alias='$alias', operators=$operators, theory=$theory, primitives=$primitives)"
}