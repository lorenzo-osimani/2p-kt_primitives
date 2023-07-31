package examples

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.data.CustomDataStore

val getEventsPrimitive = DistributedPrimitiveWrapper("testEvents", 0) { request ->
    sequence {
        val log = mutableListOf<String>()
        var flag = true

        listOf(
            Pair(Solver.prolog.defaultUnificator.context, request.context.unificator.context),
            Pair(Solver.prolog.defaultFlags, request.context.flags),
            Pair(Solver.prolog.defaultStaticKb, request.context.staticKb),
            Pair(Solver.prolog.defaultDynamicKb, request.context.dynamicKb),
            Pair(
                setOf("customLibrary", Solver.prolog.defaultBuiltins.alias),
                request.context.runtime.aliases.toSet()
            ),
            Pair(setOf("user_input", "stdin", "\$current"), request.context.inputStore),
            Pair(setOf("user_output", "stdout", "stderr", "\$current"), request.context.outputStore),
            Pair(CustomDataStore.empty(), request.context.customData),
            Pair(Substitution.empty(), request.context.substitution)
        ).forEach { pair ->
            if (pair.first != pair.second) {
                log.add(printDifference(pair.second, pair.first))
                flag = false
            }
        }

        if (!request.context.logicStackTrace.contains(Struct.of("testEvents"))) {
            flag = false
        }

        if (flag) {
            yield(request.replySuccess())
        } else {
            println(log)
            yield(request.replyError(DistributedError.ResolutionException(log.toString())))
        }
    }
}

private fun printDifference(actual: Any, expected: Any): String =
    "$actual was received instead of $expected"

fun main() {
    startService(getEventsPrimitive, 8086, "customLibrary")
}
