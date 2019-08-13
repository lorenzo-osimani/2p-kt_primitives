package it.unibo.tuprolog.solve.solver

import it.unibo.tuprolog.solve.Solve
import it.unibo.tuprolog.solve.solver.statemachine.SolverSLD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Represents a Prolog Goal solver
 *
 * @author Enrico
 */
interface Solver {

    /** Solves the provided goal, returning lazily initialized sequence of responses */
    suspend fun solve(goal: Solve.Request): Sequence<Solve.Response>

    companion object {

        /** Creates an SLD (*Selective Linear Definite*) solver */
        fun sld(executionScope: CoroutineScope = CoroutineScope(Dispatchers.Default)): Solver =
                SolverSLD(executionScope)
    }
}
