package com.github.garyttierney.ghidradbg.client.state

import kotlinx.serialization.Serializable

@Serializable
data class Register(val name: String, val index: Int, val value: Value)