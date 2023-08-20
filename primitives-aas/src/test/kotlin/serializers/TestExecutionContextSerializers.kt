package serializers

import it.unibo.tuprolog.primitives.ErrorMsg
import it.unibo.tuprolog.primitives.serialization.ParsingException
import it.unibo.tuprolog.primitives.serialization.deserializers.deserialize
import it.unibo.tuprolog.primitives.serialization.serializers.serialize
import it.unibo.tuprolog.primitives.utils.DummyContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestExecutionContextSerializers {

    @Test
    @Throws(Exception::class)
    fun testContextSerialization() {
        val serializedContext = DummyContext()
            .serialize()
        val deserializedContext = serializedContext.deserialize()
        assertEquals(DummyContext().procedure, deserializedContext.procedure)
        assertEquals(DummyContext().flags, deserializedContext.flags)
        assertEquals(DummyContext().operators, deserializedContext.operators)
    }

    @Test
    @Throws(Exception::class)
    fun testFailingSerialization() {
        assertFailsWith(
            ParsingException::class,
            "correctly failed"
        ) { ErrorMsg.getDefaultInstance().deserialize(actualContext = DummyContext()) }
    }
}
