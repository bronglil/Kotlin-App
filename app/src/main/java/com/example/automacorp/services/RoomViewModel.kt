package com.example.automacorp.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automacorp.models.RoomCommandDto
import com.example.automacorp.models.RoomDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RoomList(
    val rooms: List<RoomDto> = emptyList(),
    var error: String? = null,
    var room: RoomDto? = null,
)

class RoomViewModel : ViewModel() {
    val roomsState = MutableStateFlow(RoomList())
    fun findAll() {
        viewModelScope.launch(context = Dispatchers.IO) {
            runCatching { ApiServices.roomsApiService.findAll().execute() }
                .onSuccess {
                    val rooms = it.body() ?: emptyList()
                    roomsState.value = RoomList(rooms)
                }
                .onFailure {
                    it.printStackTrace()
                    roomsState.value = RoomList(emptyList(), it.stackTraceToString() )
                }
        }
    }

    fun findRoom(id: Long) {
        viewModelScope.launch(context = Dispatchers.IO) {
            runCatching { ApiServices.roomsApiService.findById(id).execute() }
                .onSuccess {
                    val room = it.body()
                }
                .onFailure {
                    it.printStackTrace()
                    val room = null
                }
        }
    }


    fun updateRoom(id: Long, roomDto: RoomDto) {
        val command = RoomCommandDto(
            name = roomDto.name,
            targetTemperature = roomDto.targetTemperature ?.let { Math.round(it * 10) /10.0 },
            currentTemperature = roomDto.currentTemperature,
        )
        viewModelScope.launch(context = Dispatchers.IO) {
            runCatching { ApiServices.roomsApiService.updateRoom(id, command).execute() }
                .onSuccess {
                    val room = it.body()
                }
                .onFailure {
                    it.printStackTrace()
                    val room = null
                }
        }
    }

}