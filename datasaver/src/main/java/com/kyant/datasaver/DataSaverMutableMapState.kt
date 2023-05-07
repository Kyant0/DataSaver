package com.kyant.datasaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

class DataSaverMutableMapState<K, V>(
    private val dataSaver: DataSaver<Map<K, V>>,
    private val key: String,
    private val initialValue: Map<K, V> = emptyMap(),
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY
) : MutableState<Map<K, V>>, MutableMap<K, V> {
    private val map = mutableStateOf(initialValue)

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

    override var value: Map<K, V>
        get() = map.value
        set(value) {
            doSetValue(value)
        }

    fun saveData() {
        scope.launch {
            dataSaver.saveData(key, value)
        }
    }

    fun valueChangedSinceInit() = map.value.deepEquals(initialValue)

    private fun <K, V> Map<K, V>.deepEquals(other: Map<K, V>): Boolean {
        if (size != other.size) return false
        for ((key, value) in this) {
            if (value != other[key]) return false
        }
        return true
    }

    fun remove(replacement: Map<K, V> = initialValue) {
        dataSaver.remove(key)
        map.value = replacement
    }

    private fun doSetValue(value: Map<K, V>) {
        val oldValue = map
        map.value = value
        if (oldValue != value && savePolicy == SavePolicy.IMMEDIATELY) {
            saveData()
        }
    }

    override fun component1(): Map<K, V> = value

    override fun component2(): (Map<K, V>) -> Unit = ::doSetValue

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
inline fun <reified K, reified V> rememberDataSaverMapState(
    key: String,
    initialValue: Map<K, V> = emptyMap(),
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    saver: DataSaver<Map<K, V>> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
): DataSaverMutableMapState<K, V> {
    var state: DataSaverMutableMapState<K, V>? = null
    DisposableEffect(key, savePolicy) {
        onDispose {
            state?.let {
                if (savePolicy == SavePolicy.DISPOSED && it.valueChangedSinceInit()) {
                    it.saveData()
                }
            }
        }
    }
    return remember(saver, key) {
        mutableDataSaverMapStateOf(key, initialValue, savePolicy, saver).also {
            state = it
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified K, reified V> mutableDataSaverMapStateOf(
    key: String,
    initialValue: Map<K, V> = emptyMap(),
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    saver: DataSaver<Map<K, V>> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
): DataSaverMutableMapState<K, V> {
    val data = try {
        saver.readData(key, initialValue)
    } catch (_: Exception) {
        initialValue
    }
    return DataSaverMutableMapState(saver, key, data, savePolicy)
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified K : Any, reified V : Any> saveDataSaverMapState(
    key: String,
    value: Map<K, V> = emptyMap(),
    saver: DataSaver<Map<K, V>> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
) {
    try {
        saver.saveData(key, value)
    } catch (_: Exception) {
    }
}
