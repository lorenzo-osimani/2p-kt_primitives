package examples

import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.primitives.server.session.Session

val filterKBPrimitive = DistributedPrimitiveWrapper("filterKB", 2) { request ->
    val arg1: Term = request.arguments[0]
    val arg2: Term = request.arguments[1]
    if (arg2.isVar) {
        request.context.filterStaticKb(
            filters = arrayOf(Pair(Session.KbFilter.STARTS_WITH, arg1.toString()))
        ).map {
            request.replySuccess(Substitution.of(arg2.castToVar(), it!!))
        }
    } else {
        sequenceOf(request.replyFail())
    }
}
