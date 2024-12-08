package com.example.automacorp

import AutomacorpTopAppBar
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.automacorp.models.RoomDto
import com.example.automacorp.services.RoomList
import com.example.automacorp.services.RoomViewModel
import com.example.automacorp.ui.theme.AutomacorpTheme

class RoomActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: RoomViewModel by viewModels()

        viewModel.roomsState.value = RoomList()

        val onRoomSelect: (RoomDto) -> Unit = { selectedRoom ->
            viewModel.roomsState.value = RoomList(
                rooms = viewModel.roomsState.value.rooms,
                room = selectedRoom
            )
        }

        val onRoomSave: (RoomDto) -> Unit = { roomToSave ->
            viewModel.updateRoom(roomToSave.id, roomToSave)
            Toast.makeText(
                baseContext,
                "Room ${roomToSave.name} update requested",
                Toast.LENGTH_SHORT
            ).show()
        }

        val navigateBack: () -> Unit = {
            startActivity(Intent(baseContext, MainActivity::class.java))
        }

        setContent {
            AutomacorpTheme {
                val roomsState by viewModel.roomsState.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.findAll()
                }

                Scaffold(
                    topBar = {
                        AutomacorpTopAppBar(
                            title = stringResource(
                                if (roomsState.room != null) R.string.room_detail
                                else R.string.rooms_list
                            ),
                            returnAction = navigateBack
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when {
                        roomsState.error != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = roomsState.error ?: "Unknown error",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        roomsState.room != null -> {
                            RoomDetail(
                                roomDto = roomsState.room!!,
                                onRoomUpdate = { updatedRoom ->
                                    viewModel.roomsState.value = RoomList(
                                        rooms = viewModel.roomsState.value.rooms,
                                        room = updatedRoom
                                    )
                                },
                                onBack = {
                                    viewModel.roomsState.value = RoomList()
                                    viewModel.findAll()
                                },
                                onRoomSave = onRoomSave,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        else -> {
                            RoomList(
                                rooms = roomsState.rooms,
                                onRoomClick = onRoomSelect,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}