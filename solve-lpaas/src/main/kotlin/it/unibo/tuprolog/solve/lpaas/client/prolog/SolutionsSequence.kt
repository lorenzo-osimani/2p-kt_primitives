package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.*
import it.unibo.tuprolog.solve.data.CustomData
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.lpaas.SolverGrpc
import it.unibo.tuprolog.solve.lpaas.solveMessage.SolutionID

class SolutionsSequence(private val solverID: String, private val computationID: String, private val query: String,
    channel: ManagedChannel
): Iterator<Solution> {

    private val solutionsCache: MutableMap<Int, Solution> = mutableMapOf()
    private val stub = SolverGrpc.newFutureStub(channel)
    private val struct = Struct.parse(query)

    /**
     * @param index the index of the requested solution
     * @return The solution
     */
    fun getSolution(index: Int): Solution {
        if(!solutionsCache.containsKey(index)) {
            val reply = stub.getSolution(
                SolutionID.newBuilder().setSolverID(solverID).setComputationID(computationID)
                    .setQuery(query).setIndex(index).build()
            ).get()

            val scope = Scope.of(struct.args.filter { it.isVar }.map { it.castToVar() })
            val unifiers: MutableMap<Var, Term> = mutableMapOf()
            reply.substitutionList.forEach { pair ->
                unifiers[scope.varOf(pair.`var`)] = Term.parse(pair.term)
            }

            solutionsCache[index] = if (reply.isYes) {
                Solution.yes(struct, Substitution.unifier(unifiers))
            } else if (reply.isNo) {
                Solution.no(struct)
            } else
                Solution.halt(struct, ResolutionException(
                    Throwable(reply.error.message), object : ExecutionContext by DummyInstances.executionContext {
                        override val procedure: Struct = Struct.parse(reply.query)
                        override val substitution: Substitution.Unifier = Substitution.unifier(unifiers)
                        override val logicStackTrace: List<Struct> = reply.error
                            .logicStackTraceList.map { Struct.parse(it) }
                        override val startTime: TimeInstant = reply.error.startTime
                        override val maxDuration: TimeDuration = reply.error.maxDuration
                        override val customData: CustomDataStore = CustomDataStore.empty().copy()
                    }))
        }
        return solutionsCache[index]!!
    }


    private var iteratorIndex = -1
    /**
     * @return the last element generated by iterator, null if empty
     */
    private fun getCurrentElement(): Solution? {
        return if(solutionsCache.isNotEmpty())
            solutionsCache[iteratorIndex]!!
        else null
    }

    override fun hasNext(): Boolean {
        return solutionsCache.isEmpty() || getCurrentElement()!!.isYes
    }

    override fun next(): Solution {
        return if(hasNext()) {
            return getSolution(++iteratorIndex)
        } else getCurrentElement()!!
    }
}