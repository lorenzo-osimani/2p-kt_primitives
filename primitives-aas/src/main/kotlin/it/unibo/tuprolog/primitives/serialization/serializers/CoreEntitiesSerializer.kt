package it.unibo.tuprolog.primitives.serialization.serializers

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.messages.ArgumentMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.messages.StructMsg
import it.unibo.tuprolog.solve.Signature

fun Term.serialize(): ArgumentMsg {
    val builder = ArgumentMsg.newBuilder()
    when (this) {
        is Var -> builder.setVar(this.name)
        is Truth -> builder.setFlag(this.isTrue)
        is Numeric -> builder.setNumeric(this.decimalValue.toDouble())
        is Atom -> builder.setAtom(this.value)
        is Struct -> builder.setStruct(this.serialize())
    }
    return builder.build()
}

fun Struct.serialize(): StructMsg {
    return StructMsg.newBuilder()
        .setFunctor(this.functor)
        .addAllArguments(
            this.args.map { it.serialize() }
        )
        .build()
}

fun Signature.serialize(): SignatureMsg =
    SignatureMsg.newBuilder().setName(this.name).setArity(this.arity).build()

fun buildClauseMsg(id: String, clause: Clause?): SolverMsg {
    val builder = SubResponseMsg.newBuilder().setId(id)
    if (clause != null) {
        builder.setClause(clause.serialize())
    } else {
        builder.setClause(StructMsg.getDefaultInstance())
    }
    return SolverMsg.newBuilder().setResponse(builder).build()
}
