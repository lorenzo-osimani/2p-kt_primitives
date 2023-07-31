package serializers

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.deserializers.distribuited.deserializeAsDistributed
import it.unibo.tuprolog.primitives.parsers.serializers.distribuited.serialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.utils.DummyContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.exception.HaltException
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.exception.error.DomainError
import it.unibo.tuprolog.solve.exception.error.EvaluationError
import it.unibo.tuprolog.solve.exception.error.ExistenceError
import it.unibo.tuprolog.solve.exception.error.InstantiationError
import it.unibo.tuprolog.solve.exception.error.PermissionError
import it.unibo.tuprolog.solve.exception.error.RepresentationError
import it.unibo.tuprolog.solve.exception.error.SyntaxError
import it.unibo.tuprolog.solve.exception.error.SystemError
import it.unibo.tuprolog.solve.exception.error.TypeError
import it.unibo.tuprolog.solve.exception.warning.InitializationIssue
import it.unibo.tuprolog.solve.exception.warning.MissingPredicate
import kotlin.test.Test
import kotlin.test.assertEquals

class TestErrorsSerializers {

    private fun genericSerializationTest(error: ResolutionException) {
        val distributedError = error
            .serialize()
            .deserializeAsDistributed()
        println(distributedError)
        val deserializedError = distributedError
            .serialize()
            .deserialize(actualContext = DummyContext())
        assertEquals(error.toString(), deserializedError.toString())
    }

    @Test
    @Throws(Exception::class)
    fun testDomainError() {
        val error = DomainError(
            "error",
            null,
            DummyContext(),
            DomainError.Expected.CLAUSE,
            Term.parse("a")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testEvaluationError() {
        val error = EvaluationError(
            "error",
            null,
            DummyContext(),
            EvaluationError.Type.FLOAT_OVERFLOW,
            Term.parse("34")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testExistenceError() {
        val error = ExistenceError(
            "error",
            null,
            DummyContext(),
            ExistenceError.ObjectType.OOP_CONSTRUCTOR,
            Term.parse("34")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testInstantiationError() {
        val error = InstantiationError(
            "error",
            null,
            DummyContext(),
            Var.of("X")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testPermissionError() {
        val error = PermissionError(
            "error",
            null,
            DummyContext(),
            PermissionError.Operation.ACCESS,
            PermissionError.Permission.FLAG,
            Atom.of("34")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testRepresentationError() {
        val error = RepresentationError(
            "error",
            null,
            DummyContext(),
            RepresentationError.Limit.CHARACTER,
            Atom.of("34")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testSyntaxError() {
        val error = SyntaxError(
            "error",
            null,
            DummyContext(),
            Atom.of("34")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testSystemError() {
        val error = SystemError(
            "error",
            null,
            DummyContext(),
            Atom.of("34")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testTypeError() {
        val error = TypeError(
            "error",
            null,
            DummyContext(),
            TypeError.Expected.BOOLEAN,
            Atom.of("34")
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testInitializationIssue() {
        val error = InitializationIssue(
            Struct.of("test", Term.parse("34")),
            ResolutionException(
                "hello",
                null,
                DummyContext()
            ),
            DummyContext()
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testMissingPredicate() {
        val error = MissingPredicate(
            DummyContext(),
            Signature("test", 0)
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testHaltException() {
        val error = HaltException(
            -1,
            "message",
            null,
            DummyContext()
        )
        genericSerializationTest(error)
    }

    @Test
    @Throws(Exception::class)
    fun testResolutionException() {
        val error = ResolutionException(
            "message",
            null,
            DummyContext()
        )
        genericSerializationTest(error)
    }
}
