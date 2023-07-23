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
inline fun <reified T> mutableSaveableListStateOf(
    key: String,
    initialValue: List<T> = emptyList(),
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    saver: DataSaver<List<T>> = DataSaver(
        save = { ProtoBuf.encodeToByteArray(it) },
        load = { ProtoBuf.decodeFromByteArray(it) }
    )
): MutableSaveableListState<T> {
    val data = try {
        saver.readData(key, initialValue)
    } catch (_: Exception) {
        initialValue
    }
    return MutableSaveableListState(saver, key, data, savePolicy)
}

class MutableSaveableListState<T>(
    private val dataSaver: DataSaver<List<T>>,
    private val key: String,
    private val initialValue: List<T> = emptyList(),
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY
) : MutableState<List<T>>, MutableList<T> {
    private val list = mutableStateOf(initialValue)

    override var value: List<T>
        get() = list.value
        set(value) {
            doSetValue(value)
        }

    override fun component1(): List<T> = value

    override fun component2(): (List<T>) -> Unit = ::doSetValue

    private fun doSetValue(value: List<T>) {
        list.value = value
        if (savePolicy == SavePolicy.IMMEDIATELY) {
            scope.launch {
                dataSaver.saveData(key, value)
            }
        }
    }

    override val size: Int
        get() = list.value.size

    override fun clear() {
        doSetValue(emptyList())
    }

    override fun addAll(elements: Collection<T>): Boolean {
        doSetValue(list.value + elements)
        return true
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        doSetValue(
            list.value.subList(0, index) + elements + list.value.subList(index, list.value.size)
        )
        return true
    }

    override fun add(index: Int, element: T) {
        doSetValue(
            list.value.subList(0, index) + element + list.value.subList(index, list.value.size)
        )
    }

    override fun add(element: T): Boolean {
        doSetValue(list.value + element)
        return true
    }

    override fun get(index: Int): T {
        return list.value[index]
    }

    override fun isEmpty(): Boolean {
        return list.value.isEmpty()
    }

    override fun iterator(): MutableIterator<T> {
        return list.value.toMutableList().iterator()
    }

    override fun listIterator(): MutableListIterator<T> {
        return list.value.toMutableList().listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        return list.value.toMutableList().listIterator(index)
    }

    override fun removeAt(index: Int): T {
        val removed = list.value[index]
        doSetValue(
            if (index == list.value.lastIndex) {
                list.value.subList(0, index)
            } else {
                list.value.subList(0, index) + list.value.subList(index + 1, list.value.size)
            }
        )
        return removed
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return list.value.subList(fromIndex, toIndex).toMutableList()
    }

    override fun set(index: Int, element: T): T {
        val old = list.value[index]
        doSetValue(
            if (index == list.value.lastIndex) {
                list.value.subList(0, index) + element
            } else {
                list.value.subList(0, index) + element + list.value.subList(
                    index + 1,
                    list.value.size
                )
            }
        )
        return old
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        doSetValue(list.value.filter { it in elements })
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        doSetValue(list.value.filter { it !in elements })
        return true
    }

    override fun remove(element: T): Boolean {
        doSetValue(list.value.filter { it != element })
        return true
    }

    override fun lastIndexOf(element: T): Int {
        return list.value.lastIndexOf(element)
    }

    override fun indexOf(element: T): Int {
        return list.value.indexOf(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return list.value.containsAll(elements)
    }

    override fun contains(element: T): Boolean {
        return list.value.contains(element)
    }

    fun replaceAll(elements: Collection<T>) {
        doSetValue(elements.toList())
    }

    fun remove(replacement: List<T> = initialValue) {
        dataSaver.remove(key)
        list.value = replacement
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
    }
}
