package com.github.garyttierney.ghidradbg.client.state

import kotlinx.serialization.Serializable

@Serializable
data class RelativeAddress(val base: Long, val displacement: Long)

@Serializable
data class Value(val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Value

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}