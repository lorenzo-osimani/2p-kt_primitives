package examples

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.primitives.utils.END_OF_READ_EVENT

val readerPrimitive = DistributedPrimitiveWrapper("readLine", 2) { request ->
    var line = END_OF_READ_EVENT
    sequence {
        do {
            try {
                line = request.readLine(request.arguments[0].castToAtom().toString())
                if (line != END_OF_READ_EVENT) {
                    yield(request.replySuccess(Substitution.of(request.arguments[1].castToVar(), Atom.of(line))))
                } else {
                    yield(request.replyFail())
                }
            } catch (e: Exception) {
                yield(request.replyError(DistributedError.ResolutionException(e.toString(), e)))
            }
        }
        while (line != END_OF_READ_EVENT)
    }
}

fun main() {
    startService(readerPrimitive, 8082, "customLibrary")
}
