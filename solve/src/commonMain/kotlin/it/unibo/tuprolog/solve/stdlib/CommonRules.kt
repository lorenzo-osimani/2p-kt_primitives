package it.unibo.tuprolog.solve.stdlib

import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.rule.RuleWrapper
import it.unibo.tuprolog.solve.stdlib.rule.Arrow
import it.unibo.tuprolog.solve.stdlib.rule.Member
import it.unibo.tuprolog.solve.stdlib.rule.Not
import it.unibo.tuprolog.solve.stdlib.rule.Semicolon
import it.unibo.tuprolog.theory.Theory

object CommonRules {
    val wrappers: Sequence<RuleWrapper<ExecutionContext>> = sequenceOf(
        Not,
        Arrow,
        Semicolon.If.Then,
        Semicolon.If.Else,
        Semicolon.Or.Left,
        Semicolon.Or.Right,
        Member.Base,
        Member.Recursive
    )

    val theory: Theory
        get() = Theory.indexedOf(wrappers.map { it.wrappedImplementation })
}