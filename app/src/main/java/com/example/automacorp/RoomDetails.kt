package com.example.automacorp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.automacorp.models.RoomDto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetail(
    roomDto: RoomDto,
    onRoomUpdate: (RoomDto) -> Unit = {},
    onBack: () -> Unit = {},
    onRoomSave: (RoomDto) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var room by remember { mutableStateOf(roomDto) }
    var isLoading by remember { mutableStateOf(false) }


    LaunchedEffect(isLoading) {
        if (isLoading) {
            kotlinx.coroutines.delay(3000)
            isLoading = false
        }
    }


    Column(modifier = modifier.padding(16.dp)) {
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

        Text(
            text = "Current Temperature",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${room.currentTemperature ?: "N/A"}°C",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Target Temperature",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = (room.targetTemperature ?: 18.0).toFloat(),
            onValueChange = {
                val roundedValue = (Math.round(it * 10.0) / 10.0)
                room = room.copy(targetTemperature = roundedValue)
                onRoomUpdate(room)
            },
            valueRange = 10f..28f,
            steps = 0
        )
        Text(
            text = "${Math.round((room.targetTemperature ?: 18.0) * 10) / 10}°C",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save and Back Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally)
        ) {
            // Back Button
            FilledTonalButton(
                onClick = onBack,
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Save Button
            Button(
                onClick = {
                    isLoading = true
                    onRoomSave(room)
                },
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Saving...",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Save",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}