package it.unibo.tuprolog.core.impl

import it.unibo.tuprolog.core.Cons
import it.unibo.tuprolog.core.ListIterator
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.exception.SubstitutionApplicationException
import it.unibo.tuprolog.utils.Cache

internal class ConsSubstitutionDecorator(
    head: Term,
    tail: Term,
    tags: Map<String, Any>,
    private val unifier: Substitution.Unifier
) : ConsImpl(head, tail, tags) {

    private val applyCache: Cache<Substitution.Unifier, Term> = Cache.simpleLru()

    constructor(delegate: Cons, unifier: Substitution.Unifier) :
        this(delegate.head, delegate.tail, delegate.tags, unifier)

    override val head: Term by lazy { head.apply(unifier) }

    override val tail: Term by lazy { super.tail.apply(unifier) }

    override val isGround: Boolean by lazy {
        unfoldedList.all { it.isGround }
    }

    override val args: Array<Term>
        get() = arrayOf(head, tail)

    override fun apply(substitution: Substitution): Term {
        return when (val result = substitution + unifier) {
            is Substitution.Unifier -> applyCache.getOrSet(result) {
                ConsSubstitutionDecorator(super.head, super.tail, tags, result)
            }
            else -> throw SubstitutionApplicationException(this, substitution)
        }
    }

    override val unfoldedList: List<Term>
        get() = super.unfoldedList

    override val unfoldedArray: Array<Term>
        get() = super.unfoldedArray

    override val unfoldedSequence: Sequence<Term>
        get() = Iterable { ListIterator.Substituting.All(this, unifier) }.asSequence()

    override fun copyWithTags(tags: Map<String, Any>): Cons =
        if (tags != this.tags) ConsSubstitutionDecorator(super.head, super.tail, tags, unifier) else this
}
