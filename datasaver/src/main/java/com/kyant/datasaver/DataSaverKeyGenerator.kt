package com.kyant.datasaver

import kotlin.reflect.KProperty

internal object DataSaverKeyGenerator {
    fun generate(thisRef: Any?, property: KProperty<*>): String {
        val propertyName = property.name
        return if (thisRef == null) {
            propertyName
        } else {
            "${thisRef.javaClass.name}:$propertyName"
        }
    }
}
