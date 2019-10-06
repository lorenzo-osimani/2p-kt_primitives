package it.unibo.tuprolog.libraries.stdlib

import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solve
import org.gciatto.kt.math.BigInteger

object Natural : UnaryPredicate("natural") {
    override fun uncheckedImplementation(request: Solve.Request<ExecutionContext>): Sequence<Solve.Response> =
            when (val x = request.arguments[0]) {
                is Var -> generateValues(x).map { request.replySuccess(Substitution.of(x, it)) }
                is Integer -> sequenceOf(request.replyWith(checkValue(x)))
                else -> sequenceOf(request.replyFail())
            }

    private fun generateValues(variable: Var): Sequence<Term> = sequence {
        var i = BigInteger.ZERO

        while (true) {
            yield(Integer.of(i))

            i += BigInteger.ONE
        }
    }

    private fun checkValue(value: Integer): Boolean =
            value.intValue.signum >= 0

}