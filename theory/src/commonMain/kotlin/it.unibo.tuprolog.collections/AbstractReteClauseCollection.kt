package it.unibo.tuprolog.collections

import it.unibo.tuprolog.collections.rete.generic.ReteNode
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.theory.Theory

internal abstract class AbstractReteClauseCollection<Self : AbstractReteClauseCollection<Self>>
    protected constructor(
        private val rete: ReteNode<*, Clause>
    ) : AbstractClauseCollection<Self>(rete) {

    /** Construct a Clause database from given clauses */
    constructor(
        clauses: Iterable<Clause>,
        reteNodeBuilder: (Iterable<Clause>) -> ReteNode<*, Clause>
    ) : this(reteNodeBuilder(clauses)) {
        Theory.checkClausesCorrect(clauses)
    }

    protected abstract fun newCollectionBuilder(rete: ReteNode<*, Clause>): Self

    override fun add(clause: Clause): Self =
        newCollectionBuilder(
            rete.deepCopy().apply {
                put(Theory.checkClauseCorrect(clause))
            }
        )

    override fun addAll(clauses: Iterable<Clause>): Self =
        newCollectionBuilder(
            rete.deepCopy().apply {
                put(Theory.checkClausesCorrect(clauses))
            }
        )

    override fun retrieve(clause: Clause): RetrieveResult<out Self> {
        val newTheory = rete.deepCopy()
        val retracted = newTheory.remove(clause)

        @Suppress("UNCHECKED_CAST")
        return when {
            retracted.none() ->
                RetrieveResult.Failure(this as Self)
            else ->
                RetrieveResult.Success(
                    newCollectionBuilder(newTheory), retracted.toList()
                )
        }
    }

    override fun retrieveAll(clause: Clause): RetrieveResult<out Self> {
        val newTheory = rete.deepCopy()
        val retracted = newTheory.removeAll(clause)

        @Suppress("UNCHECKED_CAST")
        return when {
            retracted.none() ->
                RetrieveResult.Failure(this as Self)
            else ->
                RetrieveResult.Success(
                    newCollectionBuilder(newTheory), retracted.toList()
                )
        }
    }
}