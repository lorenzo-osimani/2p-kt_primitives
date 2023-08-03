package it.unibo.tuprolog.primitives.utils

private const val STRING_LENGTH = 10
private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun idGenerator(): String {
    return List(STRING_LENGTH) { charPool.random() }.joinToString("")
}

const val END_OF_READ_EVENT = ""

const val TERMINATION_TIMEOUT: Long = 60

inline fun <reified T> Any?.checkType(): T {
    return if (this is T) {
        this
    } else {
        throw TypeCastException()
    }
}
