package it.unibo.tuprolog.primitives.client.impl

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.primitives.GenericGetMsg
import it.unibo.tuprolog.primitives.InspectKbMsg
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.SessionSolver
import it.unibo.tuprolog.primitives.serialization.ParsingException
import it.unibo.tuprolog.primitives.serialization.deserializers.deserialize
import it.unibo.tuprolog.primitives.serialization.serializers.buildChannelResponse
import it.unibo.tuprolog.primitives.serialization.serializers.buildClauseMsg
import it.unibo.tuprolog.primitives.serialization.serializers.buildCustomDataStoreResponse
import it.unibo.tuprolog.primitives.serialization.serializers.buildFlagStoreResponse
import it.unibo.tuprolog.primitives.serialization.serializers.buildLibrariesResponse
import it.unibo.tuprolog.primitives.serialization.serializers.buildLineMsg
import it.unibo.tuprolog.primitives.serialization.serializers.buildLogicStackTraceResponse
import it.unibo.tuprolog.primitives.serialization.serializers.buildOperatorsResponse
import it.unibo.tuprolog.primitives.serialization.serializers.buildSubSolveSolutionMsg
import it.unibo.tuprolog.primitives.serialization.serializers.buildUnificatorResponse
import it.unibo.tuprolog.primitives.utils.END_OF_READ_EVENT
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.primitive.Solve

class SessionSolverImpl(
    private val actualContext: ExecutionContext
) : SessionSolver {

    private val sessionSolver: Solver = actualContext.createSolver()

    private val computations: MutableMap<String, Iterator<Solution>> = mutableMapOf()

    private val theoryIterator: MutableMap<String, Iterator<Clause>> = mutableMapOf()

    override fun solve(id: String, event: SubSolveRequest): SolverMsg {
        val query = event.query.deserialize()
        computations.putIfAbsent(id, sessionSolver.solve(query, event.timeout).iterator())
        val solution: Solution = computations[id]!!.next()
        return buildSubSolveSolutionMsg(
            id,
            Solve.Response(solution),
            computations[id]!!.hasNext()
        )
    }

    override fun readLine(id: String, event: ReadLineMsg): SolverMsg {
        var line: String = END_OF_READ_EVENT
        sessionSolver.inputChannels[event.channelName]?.let { channel ->
            line = channel.read().orEmpty()
        }
        return buildLineMsg(id, event.channelName, line)
    }

    override fun inspectKb(id: String, event: InspectKbMsg): SolverMsg {
        if (!theoryIterator.containsKey(id)) {
            val inspectedKB = when (event.kbType) {
                InspectKbMsg.KbType.STATIC -> sessionSolver.staticKb
                InspectKbMsg.KbType.DYNAMIC -> sessionSolver.dynamicKb
                InspectKbMsg.KbType.BOTH -> sessionSolver.staticKb + sessionSolver.dynamicKb
                else -> throw ParsingException(this)
            }
            val filters = event.filtersList.map { filter ->
                when (filter.type) {
                    InspectKbMsg.FilterType.CONTAINS_FUNCTOR -> {
                        { clause: Clause ->
                            (clause.head?.functor == filter.argument) or
                                ((clause.head?.args?.plus(clause.bodyItems))?.any {
                                    println(it.toString() + " " + it.isStruct)
                                    it.isStruct && it.castToStruct().functor == filter.argument
                                } == true)
                        }
                    }
                    InspectKbMsg.FilterType.CONTAINS_TERM -> {
                        { clause: Clause ->
                            val argument = Term.parse(filter.argument)
                            (clause.head?.structurallyEquals(argument) == true) or
                                ((clause.head?.args?.plus(clause.bodyItems))?.any {
                                    it.structurallyEquals(argument)
                                } == true)
                        }
                    }
                    InspectKbMsg.FilterType.STARTS_WITH -> {
                        { clause: Clause ->
                            if (clause.head != null) {
                                clause.head!!.toString().startsWith(filter.argument, true)
                            } else {
                                false
                            }
                        }
                    }
                    else -> throw ParsingException(this)
                }
            }
            val iterator = inspectedKB.filter {
                filters.all { filter -> filter(it) }
            }
            theoryIterator[id] =
                if (event.maxClauses.toInt() == -1) {
                    iterator.iterator()
                } else {
                    iterator.take(event.maxClauses.toInt()).iterator()
                }
        }

        return buildClauseMsg(
            id,
            if (theoryIterator[id]!!.hasNext()) {
                theoryIterator[id]!!.next()
            } else {
                theoryIterator.remove(id)
                null
            }
        )
    }

    override fun getExecutionContextElement(id: String, type: GenericGetMsg.Element): SolverMsg {
        return when (type) {
            GenericGetMsg.Element.LOGIC_STACKTRACE ->
                buildLogicStackTraceResponse(id, actualContext.logicStackTrace)
            GenericGetMsg.Element.CUSTOM_DATA_STORE ->
                buildCustomDataStoreResponse(id, actualContext.customData)
            GenericGetMsg.Element.LIBRARIES ->
                buildLibrariesResponse(id, sessionSolver.libraries)
            GenericGetMsg.Element.UNIFICATOR ->
                buildUnificatorResponse(id, sessionSolver.unificator)
            GenericGetMsg.Element.FLAGS ->
                buildFlagStoreResponse(id, sessionSolver.flags)
            GenericGetMsg.Element.OPERATORS ->
                buildOperatorsResponse(id, sessionSolver.operators)
            GenericGetMsg.Element.INPUT_CHANNELS ->
                buildChannelResponse(id, sessionSolver.inputChannels.map { it.key })
            GenericGetMsg.Element.OUTPUT_CHANNELS ->
                buildChannelResponse(id, sessionSolver.outputChannels.map { it.key })
            else -> throw ParsingException(this)
        }
    }
}
