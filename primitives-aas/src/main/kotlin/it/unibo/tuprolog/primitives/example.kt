package it.unibo.tuprolog.primitives

import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory.searchLibrary
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.examples.innestedPrimitiveServer
import it.unibo.tuprolog.primitives.server.examples.ntPrimitiveServer
import it.unibo.tuprolog.primitives.server.examples.readerPrimitive
import it.unibo.tuprolog.primitives.server.examples.readerPrimitiveServer
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.library.Runtime

fun main() {
    val libraryName = "customLibrary"
    listOf(Pair(innestedPrimitiveServer, 8080), Pair(ntPrimitiveServer, 8081),
        Pair(readerPrimitiveServer, 8082)).forEach {
        Thread {
            PrimitiveServerFactory.startService(it.first, it.second, libraryName)
        }.start()
    }

    Thread.sleep(5000)

    logicProgramming {
        val solver = Solver.prolog.solverWithDefaultBuiltins(
            otherLibraries = Runtime.of(searchLibrary("customLibrary")),
            staticKb = theoryOf(
                fact { "user"("giovanni") },
            ),
            stdIn = InputChannel.of("hell")
        )
        val query = "nt"(X)
        val solutions = solver.solve(query)
        solutions.take(3).forEach {
            when (it) {
                is Solution.No -> println("no.\n")
                is Solution.Yes -> {
                    println("yes: ${it.solvedQuery}")
                    for (assignment in it.substitution) {
                        println("\t${assignment.key} / ${assignment.value}")
                    }
                    println()
                }
                is Solution.Halt -> {
                    println("halt: ${it.exception.message}")
                    for (err in it.exception.logicStackTrace) {
                        println("\t $err")
                    }
                }
            }
        }
    }
}
