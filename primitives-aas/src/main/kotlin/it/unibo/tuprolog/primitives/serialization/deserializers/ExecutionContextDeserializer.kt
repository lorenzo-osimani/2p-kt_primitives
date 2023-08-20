package it.unibo.tuprolog.primitives.serialization.deserializers

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.messages.CustomDataMsg
import it.unibo.tuprolog.primitives.messages.ExecutionContextMsg
import it.unibo.tuprolog.primitives.messages.FlagsMsg
import it.unibo.tuprolog.primitives.messages.LibrariesMsg
import it.unibo.tuprolog.primitives.messages.LibraryMsg
import it.unibo.tuprolog.primitives.messages.LogicStacktraceMsg
import it.unibo.tuprolog.primitives.messages.OperatorSetMsg
import it.unibo.tuprolog.primitives.messages.UnificatorMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistributedRuntime
import it.unibo.tuprolog.primitives.utils.DummyContext
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.unify.Unificator

fun ExecutionContextMsg.deserialize(
    scope: Scope = Scope.empty()
): ExecutionContext {
    val source = this
    return object : DummyContext() {
        override val procedure = source.procedure.deserialize(scope)
        override val substitution = Substitution.of(
            source.substitutionsMap.map
            {
                Pair(deserializeVar(it.key, scope), it.value.deserialize(scope))
            }.toMap()
        )
        override val startTime = source.startTime
        override val endTime = source.endTime
        override val remainingTime = source.remainingTime
        override val elapsedTime = source.elapsedTime
        override val maxDuration = source.maxDuration
    }
}

fun CustomDataMsg.deserialize(): CustomDataStore = CustomDataStore.empty().copy(
    this.persistentDataMap,
    this.durableDataMap,
    this.ephemeralDataMap
)

fun UnificatorMsg.deserialize(scope: Scope = Scope.empty()): Unificator =
    Unificator.naive(
        Substitution.of(
            this.unificatorMap.map {
                Pair(deserializeVar(it.key, scope), it.value.deserialize(scope))
            }.toMap()
        )
    )

fun LogicStacktraceMsg.deserialize(scope: Scope = Scope.empty()): List<Struct> =
    this.logicStackTraceList.map {
        it.deserialize(scope)
    }

fun FlagsMsg.deserialize(): FlagStore =
    FlagStore.of(
        this.flagsMap.map {
            Pair(it.key, it.value.deserialize())
        }.toMap()
    )

fun OperatorSetMsg.deserialize(): OperatorSet =
    OperatorSet(
        this.operatorsList.map {
            it.deserialize()
        }
    )

fun LibrariesMsg.deserialize(): DistributedRuntime =
    DistributedRuntime.of(
        this.librariesList.map { it.deserialize() }
    )

fun LibraryMsg.deserialize(): DistributedRuntime.DistributedLibrary =
    DistributedRuntime.DistributedLibrary(
        this.alias,
        this.primitivesList.map { it.deserialize() }.toSet(),
        this.rulesSignaturesList.map { it.deserialize() }.toSet(),
        this.clausesList.map { it.deserialize().asClause()!! }.toSet(),
        this.functionsSignaturesList.map { it.deserialize() }.toSet()
    )
