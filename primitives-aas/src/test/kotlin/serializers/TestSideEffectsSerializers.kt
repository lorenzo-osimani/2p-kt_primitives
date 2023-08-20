package serializers

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.serialization.deserializers.deserialize
import it.unibo.tuprolog.primitives.serialization.serializers.serialize
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.sideffects.SideEffect
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSideEffectsSerializers {

    private fun genericSerializationTest(sideEffect: SideEffect) {
        val result = sideEffect.serialize().deserialize()
        assertEquals(sideEffect.toString(), result.toString())
    }

    private val listOfClauses = listOf(
        Clause.of(
            Struct.of("test", Atom.of("1")),
            Struct.of("hello", Atom.of("2"))
        )
    )

    @Test
    @Throws(Exception::class)
    fun testAddStaticClauses() {
        val sideEffect = SideEffect.AddStaticClauses(
            listOfClauses,
            false
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveStaticClauses() {
        val sideEffect = SideEffect.RemoveStaticClauses(
            listOfClauses
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testResetStaticKb() {
        val sideEffect = SideEffect.ResetStaticKb(
            listOfClauses
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testAddDynamicClauses() {
        val sideEffect = SideEffect.AddDynamicClauses(
            listOfClauses,
            false
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveDynamicClauses() {
        val sideEffect = SideEffect.RemoveDynamicClauses(
            listOfClauses
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testResetDynamicKb() {
        val sideEffect = SideEffect.ResetDynamicKb(
            listOfClauses
        )
        genericSerializationTest(sideEffect)
    }

    private val flags = listOf(
        Pair("flag1", Numeric.of(0)),
        Pair("flag2", Numeric.of(1))
    )

    @Test
    @Throws(Exception::class)
    fun testSetFlags() {
        val sideEffect = SideEffect.SetFlags(
            flags
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testResetFlags() {
        val sideEffect = SideEffect.ResetFlags(
            flags
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testClearFlags() {
        val sideEffect = SideEffect.ClearFlags(
            flags.map { it.first }
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testUnloadLibraries() {
        val sideEffect = SideEffect.UnloadLibraries(
            "customLibrary"
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testSetOperators() {
        val sideEffect = SideEffect.SetOperators(
            Solver.prolog.defaultBuiltins.operators.take(1)
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveOperators() {
        val sideEffect = SideEffect.RemoveOperators(
            Solver.prolog.defaultBuiltins.operators.take(1)
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testResetOperators() {
        val sideEffect = SideEffect.ResetOperators(
            Solver.prolog.defaultBuiltins.operators.take(1)
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testCloseInputChannels() {
        val sideEffect = SideEffect.CloseInputChannels(
            "stdin"
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testCloseOutputChannels() {
        val sideEffect = SideEffect.CloseOutputChannels(
            listOf("stdout", "\$current")
        )
        genericSerializationTest(sideEffect)
    }

    private val data = listOf(
        Pair("test1", 1),
        Pair("test2", 2)
    )

    @Test
    @Throws(Exception::class)
    fun testSetPersistentData() {
        val sideEffect = SideEffect.SetPersistentData(
            data
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testSetDurableData() {
        val sideEffect = SideEffect.SetDurableData(
            data
        )
        genericSerializationTest(sideEffect)
    }

    @Test
    @Throws(Exception::class)
    fun testSetEphemeralData() {
        val sideEffect = SideEffect.SetEphemeralData(
            data
        )
        genericSerializationTest(sideEffect)
    }
}
