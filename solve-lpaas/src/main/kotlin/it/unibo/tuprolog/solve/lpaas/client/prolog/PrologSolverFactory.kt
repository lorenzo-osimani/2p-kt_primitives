package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.SolverFactoryGrpc
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.SolverId
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.SolverRequest
import it.unibo.tuprolog.solve.lpaas.util.parsers.*
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator


object PrologSolverFactory  {

    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()
    private val stub = SolverFactoryGrpc.newFutureStub(channel)

    fun solverOf(unificator: Unificator = Unificator.strict(), libraries: Set<String> = emptySet(),
                 flags: FlagStore = FlagStore.empty(), staticKb: Theory = Theory.empty(),
                 dynamicKb: Theory = Theory.empty(), inputs: Map<String, String> = emptyMap(),
                 outputs: Set<String> = emptySet(), defaultBuiltins: Boolean = true): ClientSolver {
        return ClientPrologSolverImpl(
            generateSolverID(unificator, libraries, flags, staticKb, dynamicKb,
            inputs, outputs, defaultBuiltins), channel
        )
    }

    fun mutableSolverOf(unificator: Unificator = Unificator.strict(), libraries: Set<String> = emptySet(),
                         flags: FlagStore = FlagStore.empty(), staticKb: Theory = Theory.empty(),
                         dynamicKb: Theory = Theory.empty(), inputs: Map<String, String> = emptyMap(),
                         outputs: Set<String> = emptySet(), defaultBuiltins: Boolean = true): ClientMutableSolver {
        return ClientPrologMutableSolverImpl(
            generateSolverID(unificator, libraries, flags, staticKb, dynamicKb,
                inputs, outputs, defaultBuiltins, true), channel
        )
    }

    fun connectToSolver(solverId: String): ClientSolver? {
        val result = stub.connectToSolver(SolverId.newBuilder().setId(solverId).build()).get()
        return if(result.result) {
            ClientPrologSolverImpl(solverId, channel)
        } else {
            null
        }
    }

    /**Add check if its is mutable*/
    fun connectToMutableSolver(solverId: String): ClientMutableSolver? {
        val result = stub.connectToSolver(SolverId.newBuilder().setId(solverId).build()).get()
        return if(result.result) {
            ClientPrologMutableSolverImpl(solverId, channel)
        } else {
            null
        }
    }

    private fun generateSolverID(unificator: Unificator, libraries: Set<String>,
                                     flags: FlagStore, staticKb: Theory, dynamicKb: Theory,
                                     inputChannels: Map<String, String>, outputChannels: Set<String>,
                                     defaultBuiltins: Boolean, mutable: Boolean = false): String {
        val createSolverRequest: SolverRequest = SolverRequest.newBuilder()
            .setUnificator(fromUnificatorToMsg(unificator))
            .setRuntime(fromLibrariesToMsg(libraries))
            .setFlags(fromFlagsToMsg(flags))
            .setStaticKb(fromTheoryToMsg(staticKb))
            .setDynamicKb(fromTheoryToMsg(dynamicKb))
            .setInputStore(fromChannelsToMsg(inputChannels))
            .setOutputStore(fromChannelsToMsg(outputChannels))
            .setDefaultBuiltIns(defaultBuiltins)
            .setMutable(mutable).build()
        return stub.solverOf(createSolverRequest).get().id
    }
}
