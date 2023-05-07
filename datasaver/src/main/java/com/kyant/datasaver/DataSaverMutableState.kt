package com.kyant.datasaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

class DataSaverMutableState<T>(
    private val dataSaver: DataSaver<T>,
    private val key: String,
    private val initialValue: T,
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY
) : MutableState<T> {
    private val state = mutableStateOf(initialValue)

    override var value: T
        get() = state.value
        set(value) {
            doSetValue(value)
        }

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        doSetValue(value)
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = state.value

    fun saveData() {
        value?.let {
            scope.launch {
                dataSaver.saveData(key, it)
            }
        } ?: run {
            dataSaver.remove(key)
        }
    }

    fun remove(replacement: T = initialValue) {
        dataSaver.remove(key)
        state.value = replacement
    }

    fun valueChangedSinceInit() = state.value != initialValue

    private fun doSetValue(value: T) {
        val oldValue = this.state.value
        this.state.value = value
        if (oldValue != value && savePolicy == SavePolicy.IMMEDIATELY) {
            saveData()
        }
    }

    override operator fun component1() = state.value

    override operator fun component2(): (T) -> Unit = ::doSetValue

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
inline fun <reified T> rememberDataSaverState(
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    saver: DataSaver<T> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
): DataSaverMutableState<T> {
    var state: DataSaverMutableState<T>? = null
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
        mutableDataSaverStateOf(key, initialValue, savePolicy, saver).also {
            state = it
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> mutableDataSaverStateOf(
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    saver: DataSaver<T> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
): DataSaverMutableState<T> {
    val data = try {
        saver.readData(key, initialValue)
    } catch (_: Exception) {
        initialValue
    }
    return DataSaverMutableState(saver, key, data, savePolicy)
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> saveDataSaverState(
    key: String,
    value: T,
    saver: DataSaver<T> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
) {
    try {
        saver.saveData(key, value)
    } catch (_: Exception) {
    }
}
