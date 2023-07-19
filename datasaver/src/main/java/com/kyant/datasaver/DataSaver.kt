package com.kyant.datasaver

import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class DataSaver<T>(
    private val save: (T) -> ByteArray,
    private val load: (ByteArray) -> T
) {
    private val lock = ReentrantReadWriteLock()

    fun saveData(key: String, data: T) {
        return try {
            lock.write {
                val file = File(path, key)
                file.writeBytes(save(data))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readData(key: String, default: T): T {
        return try {
            lock.read {
                val file = File(path, key)
                if (file.exists()) {
                    load(file.readBytes())
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: default
    }

    fun remove(key: String) {
        try {
            File(path, key).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private lateinit var path: String

        fun init(location: String) {
            this.path = location
        }
    }
}
