package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Constant
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.primitives.messages.ArgumentMsg
import it.unibo.tuprolog.primitives.messages.OperatorMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.messages.StructMsg
import it.unibo.tuprolog.primitives.messages.TheoryMsg
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.theory.Theory

fun ArgumentMsg.deserialize(scope: Scope = Scope.empty()): Term =
    if (this.hasVar()) {
        deserializeVar(this.`var`, scope)
    } else if (this.hasStruct()) {
        this.struct.deserialize()
    } else if (this.hasFlag()) {
        Truth.of(this.flag)
    } else if (this.hasNumeric()) {
        val value = this.numeric
        if (value % 1 > 0) {
            Constant.parse(this.numeric.toString())
        } else {
            Constant.parse(this.numeric.toInt().toString())
        }
    } else {
        Atom.of(this.atom)
    }

fun deserializeVar(name: String, scope: Scope = Scope.empty()): Var =
    scope.varOf(name)

fun StructMsg.deserialize(scope: Scope = Scope.empty()): Struct =
    Struct.of(this.functor, this.argumentsList.map { it.deserialize(scope) })

fun StructMsg.deserializeAsClause(scope: Scope = Scope.empty()): Clause? =
    if (this != StructMsg.getDefaultInstance()) {
        this.deserialize(scope).asClause()
    } else {
        null
    }

fun SignatureMsg.deserialize(): Signature = Signature(this.name, this.arity)

fun OperatorMsg.deserialize(): Operator =
    Operator(this.functor, Specifier.valueOf(this.specifier), this.priority)

fun TheoryMsg.deserialize(): Theory =
    Theory.of(
        this.clausesList.map {
            it.deserialize().castToClause()
        }
    )
