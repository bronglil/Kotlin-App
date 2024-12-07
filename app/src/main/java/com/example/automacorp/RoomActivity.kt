package com.example.automacorp

import AutomacorpTopAppBar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.automacorp.models.RoomCommandDto
import com.example.automacorp.models.RoomDto
import com.example.automacorp.services.ApiServices
import com.example.automacorp.ui.theme.AutomacorpTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.round
import java.text.DecimalFormat

class RoomViewModel : ViewModel() {
    var room by mutableStateOf<RoomDto?>(null)
}

fun formatTemperature(value: Double): String {
    val decimalFormat = DecimalFormat("#.##")
    return decimalFormat.format(value) + "°C"
}

class RoomActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: RoomViewModel by viewModels()

        val onRoomSelect: (RoomDto) -> Unit = { selectedRoom ->
            viewModel.room = selectedRoom
        }

        val onRoomSave: () -> Unit = {
            viewModel.room?.let { room ->
                val roomCommand = RoomCommandDto(name = room.name,
                    currentTemperature = room.currentTemperature,
                    targetTemperature = room.targetTemperature
                )

                ApiServices.roomsApiService.updateRoom(room.id, roomCommand)
                    .enqueue(object : Callback<RoomDto> {
                        override fun onResponse(call: Call<RoomDto>, response: Response<RoomDto>) {
                            if (response.isSuccessful) {
                                Toast.makeText(baseContext, "Room ${room.name} was updated", Toast.LENGTH_LONG).show()
                                viewModel.room = response.body() // Update with the response body
                            } else {
                                Toast.makeText(baseContext, "Error updating room", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<RoomDto>, t: Throwable) {
                            Toast.makeText(baseContext, "Failed to update room", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }

        val navigateBack: () -> Unit = {
            startActivity(Intent(baseContext, MainActivity::class.java))
        }

        // Fetch rooms from the API when activity is created
        ApiServices.roomsApiService.findAll().enqueue(object : Callback<List<RoomDto>> {
            override fun onResponse(call: Call<List<RoomDto>>, response: Response<List<RoomDto>>) {

                // Log the status code and headers for the response
                Log.d("Retrofit Response", "Status Code: ${response.code()}")
                Log.d("Retrofit Response", "Response Headers: ${response.headers()}")

                if (response.isSuccessful) {
                    val rooms = response.body() ?: emptyList()
                    Log.d("Fetched Rooms", rooms.toString())

                    // Continue with the UI update
                    setContent {
                        AutomacorpTheme {
                            Scaffold(
                                topBar = { AutomacorpTopAppBar("Room", navigateBack) },
                                modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->
                                if (viewModel.room == null) {
                                    RoomList(
                                        rooms = rooms,
                                        onRoomClick = onRoomSelect,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                } else {
                                    RoomDetail(
                                        model = viewModel,
                                        roomDto = viewModel.room!!,
                                        onRoomUpdate = { updatedRoom ->
                                            viewModel.room = updatedRoom
                                        },
                                        onBack = {
                                            viewModel.room = null
                                        },
                                        onRoomSave = onRoomSave,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Log.e("Retrofit Error", "Error Response: ${response.errorBody()?.string()}")
                    Toast.makeText(baseContext, "Failed to load rooms", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<RoomDto>>, t: Throwable) {
                Log.e("API Call Error", "Error: ${t.message}")
                Toast.makeText(baseContext, "Failed to load rooms", Toast.LENGTH_LONG).show()
            }
        })

    }

    @Composable
    fun RoomList(
        rooms: List<RoomDto>,
        onRoomClick: (RoomDto) -> Unit,
        modifier: Modifier = Modifier
    ) {
        if (rooms.isEmpty()) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No rooms available",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(rooms) { room ->
                    RoomListItem(
                        room = room,
                        onClick = { onRoomClick(room) }
                    )
                }
            }
        }
    }

    @Composable
    fun RoomListItem(
        room: RoomDto,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = room.name ?: "Unnamed Room",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = "Current Temp: ${room.currentTemperature?.let { formatTemperature(it) } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Target Temp: ${room.targetTemperature?.let { formatTemperature(it) } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RoomDetail(
        model: RoomViewModel,
        roomDto: RoomDto,
        onRoomUpdate: (RoomDto) -> Unit = {},
        onBack: () -> Unit = {},
        onRoomSave: () -> Unit = {},
        modifier: Modifier = Modifier
    ) {
        var room by remember { mutableStateOf(roomDto) }

        Column(modifier = modifier.padding(16.dp)) {
            // Room Name TextField
            OutlinedTextField(
                value = room.name ?: "",
                onValueChange = {
                    room = room.copy(name = it)
                    onRoomUpdate(room)
                },
                label = { Text(text = "Room Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current Temperature Display
            Text(
                text = "Current Temperature",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${room.currentTemperature ?: "N/A"}°C",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Temperature Slider
            Text(
                text = "Target Temperature",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = (room.targetTemperature ?: 18.0).toFloat(),
                onValueChange = {
                    val roundedValue = (round(it * 10.0) / 10.0)
                    room = room.copy(targetTemperature = roundedValue)
                    onRoomUpdate(room)
                },
                valueRange = 10f..28f,
                steps = 0
            )
            Text(
                text = "${round((room.targetTemperature ?: 18.0) * 10) / 10}°C",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save and Back Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExtendedFloatingActionButton(
                    onClick = onBack,
                    icon = {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    },
                    text = { Text(text = "Back") }
                )

                Spacer(modifier = Modifier.width(16.dp))

                ExtendedFloatingActionButton(
                    onClick = { onRoomSave() },
                    icon = {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Save",
                        )
                    },
                    text = { Text(text = "Save") }
                )
            }
        }
    }
}
