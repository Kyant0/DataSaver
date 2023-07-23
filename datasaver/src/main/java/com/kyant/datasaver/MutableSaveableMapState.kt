package com.kyant.datasaver

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified K, reified V> mutableSaveableMapStateOf(
    key: String,
    initialValue: Map<K, V> = emptyMap(),
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    saver: DataSaver<Map<K, V>> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
): MutableSaveableMapState<K, V> {
    val data = try {
        saver.readData(key, initialValue)
    } catch (_: Exception) {
        initialValue
    }
    return MutableSaveableMapState(saver, key, data, savePolicy)
}

class MutableSaveableMapState<K, V>(
    private val dataSaver: DataSaver<Map<K, V>>,
    private val key: String,
    private val initialValue: Map<K, V> = emptyMap(),
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY
) : MutableState<Map<K, V>>, MutableMap<K, V> {
    private val map = mutableStateOf(initialValue)

    override var value: Map<K, V>
        get() = map.value
        set(value) {
            doSetValue(value)
        }

    override fun component1(): Map<K, V> = value

    override fun component2(): (Map<K, V>) -> Unit = ::doSetValue

    private fun doSetValue(value: Map<K, V>) {
        map.value = value
        if (savePolicy == SavePolicy.IMMEDIATELY) {
            scope.launch {
                dataSaver.saveData(key, value)
            }
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.value.toMutableMap().entries
    override val keys: MutableSet<K>
        get() = map.value.keys.toMutableSet()
    override val size: Int
        get() = map.value.size
    override val values: MutableCollection<V>
        get() = map.value.values.toMutableList()

    override fun clear() {
        doSetValue(emptyMap())
    }

    override fun isEmpty(): Boolean {
        return map.value.isEmpty()
    }

    override fun remove(key: K): V? {
        val oldValue = map.value[key]
        doSetValue(map.value - key)
        return oldValue
    }

    override fun putAll(from: Map<out K, V>) {
        doSetValue(map.value + from)
    }

    override fun put(key: K, value: V): V? {
        val oldValue = map.value[key]
        doSetValue(map.value + (key to value))
        return oldValue
    }

    override fun get(key: K): V? {
        return map.value[key]
    }

    override fun containsValue(value: V): Boolean {
        return map.value.containsValue(value)
    }

    override fun containsKey(key: K): Boolean {
        return map.value.containsKey(key)
    }

    fun replaceAll(elements: Map<K, V>) {
        doSetValue(elements.toMap())
    }

    fun remove(replacement: Map<K, V> = initialValue) {
        dataSaver.remove(key)
        map.value = replacement
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
    }
}
