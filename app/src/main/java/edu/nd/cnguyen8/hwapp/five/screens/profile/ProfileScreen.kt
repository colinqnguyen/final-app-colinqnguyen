package edu.nd.cnguyen8.hwapp.five.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val expanded = remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile")

        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .size(120.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            val imageModel = viewModel.selectedImageUri ?: viewModel.profileImageUrl.takeIf { it.isNotBlank() }

            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image")
                }
            }

            Surface(
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture"
                    )
                }
            }
        }

        OutlinedTextField(
            value = viewModel.nickname,
            onValueChange = viewModel::onNicknameChange,
            label = { Text("Nickname") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )

        OutlinedTextField(
            value = viewModel.greeting,
            onValueChange = viewModel::onGreetingChange,
            label = { Text("Greeting") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = !expanded.value },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = viewModel.shirtColor,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Shirt Color") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    viewModel.shirtColorOptions.forEach { color ->
                        DropdownMenuItem(
                            text = { Text(color) },
                            onClick = {
                                viewModel.onShirtColorChange(color)
                                expanded.value = false
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(width = 44.dp, height = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shirtColorToColor(viewModel.shirtColor))
            )
        }

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (viewModel.saveMessage != null) {
            Text(
                text = viewModel.saveMessage!!,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.saveProfile() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save Profile")
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Back")
        }
    }
}

private fun shirtColorToColor(name: String): Color {
    return when (name) {
        "Blue" -> Color.Blue
        "Red" -> Color.Red
        "Green" -> Color.Green
        "Yellow" -> Color.Yellow
        "Black" -> Color.Black
        "White" -> Color.White
        "Purple" -> Color(0xFF9C27B0)
        "Orange" -> Color(0xFFFF9800)
        else -> Color.Blue
    }
}