package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.SolutionMsg
import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution

fun SolutionMsg.deserialize(scope: Scope = Scope.empty(), actualContext: ExecutionContext): Solution {
    val query = this.query.deserialize(scope)
    return when (this.type) {
        SolutionMsg.SolutionType.SUCCESS -> {
            val substitution = Substitution.of(
                this.substitutionsMap.map {
                    Pair(
                        scope.varOf(it.key),
                        it.value.deserialize(scope)
                    )
                }
            ).asUnifier()
            if (substitution != null) {
                Solution.yes(query, substitution)
            } else Solution.yes(query)
        }
        SolutionMsg.SolutionType.FAIL -> {
            Solution.no(query)
        }
        SolutionMsg.SolutionType.HALT -> {
            Solution.halt(query, this.error.deserialize(scope, actualContext))
        }
        else ->
            throw ParsingException(this)
    }
}
