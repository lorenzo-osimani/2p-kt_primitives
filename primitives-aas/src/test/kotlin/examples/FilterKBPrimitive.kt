package examples

import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.primitives.server.session.Session

val filterKBPrimitive = DistributedPrimitiveWrapper("filterKB", 3) { request ->
    val filter: Term = request.arguments[0]
    val type: Term = request.arguments[1]
    val arg2: Term = request.arguments[2]
    if (type.isAtom && arg2.isVar) {
        val filters = arrayOf(Pair(Session.KbFilter.valueOf(type.castToAtom().value), filter.toString()))
        (request.context.filterStaticKb(filters = filters) +
            request.context.filterDynamicKb(filters = filters))
            .map {
                request.replySuccess(Substitution.of(arg2.castToVar(), it!!))
            }
    } else {
        sequenceOf(request.replyFail())
    }
}
