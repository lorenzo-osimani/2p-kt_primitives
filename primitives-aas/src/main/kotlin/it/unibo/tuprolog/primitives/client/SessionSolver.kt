package it.unibo.tuprolog.primitives.client

import it.unibo.tuprolog.primitives.GenericGetMsg
import it.unibo.tuprolog.primitives.InspectKbMsg
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.impl.SessionSolverImpl
import it.unibo.tuprolog.solve.ExecutionContext

interface SessionSolver {

    /** Solve a query requested by the primitive server and sends the result.
     *  It can be blocking */
    fun solve(id: String, event: SubSolveRequest): SolverMsg

    /** Reads a character from an Input channel and sends it to the Primitive Server.
     *  It returns 'failed' if the read fails.
     */
    fun readLine(id: String, event: ReadLineMsg): SolverMsg

    /** Inspect a Kb with eventual filters and returns a filtered Theory
     */
    fun inspectKb(id: String, event: InspectKbMsg): SolverMsg

    /** Returns a specific element of the execution context
     */
    fun getExecutionContextElement(id: String, type: GenericGetMsg.Element): SolverMsg

    companion object {
        fun of(
            executionContext: ExecutionContext
        ): SessionSolverImpl =
            SessionSolverImpl(executionContext)
    }
}
