package com.example.automacorp.models

import WindowDto

data class RoomDto(
    val id: Long,
    var name: String,
    val currentTemperature: Double?,
    val targetTemperature: Double?,
    val windows: List<WindowDto>,
)

data class RoomCommandDto(
    val name: String,
    val currentTemperature: Double?,
    val targetTemperature: Double?,
    val floor: Int = 1,
    val buildingId: Long = -10
)
