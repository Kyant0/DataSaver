package com.kyant.datasaver

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.reflect.KProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> mutableSaveableStateOf(
    initialValue: T,
    saver: DataSaver<T> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
): MutableSaveableState<T> = MutableSaveableState(saver, initialValue)

class MutableSaveableState<T>(
    private val dataSaver: DataSaver<T>,
    private val initialValue: T
) : MutableState<T> {
    private lateinit var key: String
    private lateinit var state: MutableState<T>

    override var value: T
        get() = state.value
        set(_) = error("MutableSaveableState can't be set directly, use delegated property instead")

    override fun component1() = value

    override fun component2() = error("MutableSaveableState can't be deconstructed, use delegated property instead")

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        init(thisObj, property)
        state.value = value
        value?.let {
            scope.launch {
                dataSaver.saveData(key, it)
            }
        } ?: run {
            dataSaver.remove(key)
        }
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T {
        init(thisObj, property)
        return state.value
    }

    private fun init(thisObj: Any?, property: KProperty<*>) {
        if (!::key.isInitialized) {
            key = DataSaverKeyGenerator.generate(thisObj, property)
            state = mutableStateOf(
                try {
                    dataSaver.readData(key, initialValue)
                } catch (_: Exception) {
                    initialValue
                }
            )
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
    }
}
