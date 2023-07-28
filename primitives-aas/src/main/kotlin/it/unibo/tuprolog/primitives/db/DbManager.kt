package it.unibo.tuprolog.primitives.db

import it.unibo.tuprolog.solve.Signature

interface DbManager {
    data class SerializedPrimitive(
        val functor: String,
        val arity: Int,
        val url: String,
        val port: Int,
        val libraryName: String = ""
    )

    fun addPrimitive(
        signature: Signature,
        url: String = "localhost",
        port: Int = 8080,
        libraryName: String = ""
    ) =
        addPrimitive(signature.name, signature.arity, url, port, libraryName)

    fun addPrimitive(
        functor: String,
        arity: Int,
        url: String = "localhost",
        port: Int = 8080,
        libraryName: String = ""
    )

    fun getPrimitive(signature: Signature): Pair<String, Int>? = getPrimitive(signature.name, signature.arity)

    fun getPrimitive(functor: String, arity: Int): Pair<String, Int>?

    fun deletePrimitive(signature: Signature, libraryName: String) =
        deletePrimitive(signature.name, signature.arity, libraryName)

    fun deletePrimitive(functor: String, arity: Int, libraryName: String)

    fun getLibrary(libraryName: String): Set<Pair<String, Int>>

    companion object {
        private var manager: DbManager? = null

        private const val port = 27017
        private const val URL_LOCAL = "mongodb://localhost"

        init {
            manager = DbManagerImpl(URL_LOCAL, port)
        }

        fun get(): DbManager {
            if (manager != null) {
                return manager!!
            } else throw IllegalStateException("Must be initialized first")
        }
    }
}
