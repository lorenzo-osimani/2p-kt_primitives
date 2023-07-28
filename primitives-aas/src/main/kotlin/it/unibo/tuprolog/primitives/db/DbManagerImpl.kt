package it.unibo.tuprolog.primitives.db

import com.mongodb.ConnectionString
import com.mongodb.MongoException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.litote.kmongo.KMongo
import org.litote.kmongo.SetTo
import org.litote.kmongo.deleteOne
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.updateOne
import org.litote.kmongo.getCollection

class DbManagerImpl(url: String, port: Int) : DbManager {
    data class SerializedPrimitive(
        val functor: String,
        val arity: Int,
        val url: String,
        val port: Int,
        val libraryName: String = ""
    )

    private val primitivesDB: MongoCollection<SerializedPrimitive>

    init {
        val db = KMongo.createClient(ConnectionString("$url:$port"))
            .getDatabase("primitives")
        primitivesDB = db.getCollection<SerializedPrimitive>()
    }

    override fun addPrimitive(
        functor: String,
        arity: Int,
        url: String,
        port: Int,
        libraryName: String
    ) {
        if (getPrimitive(functor, arity) == null) {
            primitivesDB.insertOne(SerializedPrimitive(functor, arity, url, port, libraryName))
        } else {
            // To choose between error and update
            primitivesDB.updateOne(
                Filters.and(
                    SerializedPrimitive::functor eq functor,
                    SerializedPrimitive::arity eq arity,
                    SerializedPrimitive::libraryName eq libraryName
                ),
                SetTo(SerializedPrimitive::url, url),
                SetTo(SerializedPrimitive::port, port)
            )
        }
    }

    override fun getPrimitive(functor: String, arity: Int): Pair<String, Int>? {
        val result =
            primitivesDB.find(SerializedPrimitive::functor eq functor, SerializedPrimitive::arity eq arity).first()
        return if (result != null) {
            Pair(result.url, result.port)
        } else {
            null
        }
    }

    override fun deletePrimitive(functor: String, arity: Int, libraryName: String) {
        primitivesDB
            .deleteOne(
                SerializedPrimitive::functor eq functor,
                SerializedPrimitive::arity eq arity,
                SerializedPrimitive::libraryName eq libraryName
            )
    }

    override fun getLibrary(libraryName: String): Set<Pair<String, Int>> {
        val result =
            primitivesDB.find(SerializedPrimitive::libraryName eq libraryName)
        return result.map {
            Pair(it.functor, it.arity)
        }.toSet()
    }
}
