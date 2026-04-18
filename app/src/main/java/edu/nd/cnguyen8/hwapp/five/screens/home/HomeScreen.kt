package edu.nd.cnguyen8.hwapp.five.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.LaunchedEffect
import androidx.health.connect.client.PermissionController
import androidx.compose.material3.LinearProgressIndicator

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onGameClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    if (viewModel.showInfoDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeInfoDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.closeInfoDialog() }) {
                    Text("OK")
                }
            },
            title = {
                Text("How StepQuest Works")
            },
            text = {
                Text(
                    "You earn 1 coin for every 2000 steps. " +
                            "Your daily goal helps track your streak. " +
                            "Scroll down to see more stats."
                )
            }
        )
    }

    if (viewModel.showGoalDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeGoalDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.saveDailyGoal() }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeGoalDialog() }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Edit Daily Goal")
            },
            text = {
                OutlinedTextField(
                    value = viewModel.goalInput,
                    onValueChange = viewModel::onGoalInputChange,
                    label = { Text("Daily Goal") }
                )
            }
        )
    }

    if (viewModel.showAddStepsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddStepsDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.addCustomTestSteps() }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddStepsDialog() }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Add Test Steps")
            },
            text = {
                OutlinedTextField(
                    value = viewModel.testStepsInput,
                    onValueChange = viewModel::onTestStepsInputChange,
                    label = { Text("Number of steps") }
                )
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(viewModel.requiredHealthPermissions())) {
            viewModel.refreshHealthPermissionState()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshHealthPermissionState()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopActionRow(
                tokens = viewModel.tokens,
                onLogout = {
                    viewModel.logout()
                    onLogout()
                },
                onProfileClick = onProfileClick,
                onInfoClick = viewModel::openInfoDialog
            )
        }

        item {
            DailyProgressCard(
                currentSteps = viewModel.displayedSteps,
                dailyGoal = viewModel.dailyGoal,
                stepsUntilNextCoin = viewModel.stepsUntilNextCoin,
                goalCompletedToday = viewModel.goalCompletedToday,
                goalProgress = viewModel.goalProgress,
                onEditGoal = viewModel::openGoalDialog,
                healthConnectAvailable = viewModel.healthConnectAvailable,
                hasHealthPermission = viewModel.hasHealthPermission,
                onConnectHealth = {
                    permissionLauncher.launch(viewModel.requiredHealthPermissions())
                },
                onAddTestSteps = { viewModel.openAddStepsDialog() },
                onResetDebugSteps = { viewModel.resetDebugSteps() }
            )
        }

        item {
            GameSection(
                onPlayClick = onGameClick
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scroll for stats",
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Scroll Down",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            StatsSection(
                streakCount = viewModel.streakCount,
                caloriesBurnedToday = viewModel.caloriesBurnedToday,
                currentSteps = viewModel.displayedSteps,
                averageSteps = viewModel.averageSteps
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopActionRow(
    tokens: Int,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularActionButton(
            onClick = onLogout,
            icon = {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
            }
        )

        CircularActionButton(
            onClick = onProfileClick,
            icon = {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
            }
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🪙")
                Text(tokens.toString(), fontWeight = FontWeight.Bold)
            }
        }

        CircularActionButton(
            onClick = onInfoClick,
            icon = {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }
        )
    }
}

@Composable
private fun CircularActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        shape = CircleShape,
        tonalElevation = 4.dp
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            icon()
        }
    }
}

@Composable
private fun DailyProgressCard(
    currentSteps: Int,
    dailyGoal: Int,
    stepsUntilNextCoin: Int,
    goalCompletedToday: Boolean,
    goalProgress: Float,
    onEditGoal: () -> Unit,
    healthConnectAvailable: Boolean,
    hasHealthPermission: Boolean,
    onConnectHealth: () -> Unit,
    onAddTestSteps: () -> Unit,
    onResetDebugSteps: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "$currentSteps / $dailyGoal",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "steps",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "1 coin every 2000 steps",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = if (stepsUntilNextCoin == 0) {
                            "Coin ready at this milestone"
                        } else {
                            "$stepsUntilNextCoin steps until next coin"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = if (goalCompletedToday) {
                            "Daily goal completed"
                        } else {
                            "Daily goal not reached yet"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }

                IconButton(onClick = onEditGoal) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Daily Goal")
                }
            }

            if (!healthConnectAvailable) {
                Text(
                    text = "Health Connect is not available on this device.",
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else if (!hasHealthPermission) {
                Button(
                    onClick = onConnectHealth,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Connect Health Connect")
                }
            }

            if (healthConnectAvailable && hasHealthPermission) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddTestSteps
                    ) {
                        Text("Add Steps (test)")
                    }

                    Button(
                        onClick = onResetDebugSteps
                    ) {
                        Text("Reset Test Steps")
                    }
                }
            }
        }
    }
}

@Composable
private fun GameSection(
    onPlayClick: () -> Unit
) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CardDefaults.cardColors().containerColor),
                contentAlignment = Alignment.Center
            ) {
                Text("Game Visual Placeholder")
            }
        }

        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play"
            )
            Text(
                text = "Play",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun StatsSection(
    streakCount: Int,
    caloriesBurnedToday: Int,
    currentSteps: Int,
    averageSteps: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(title = "Steps Per Hour") {
            GraphPlaceholder("Hourly steps graph placeholder")
        }

        StatCard(title = "Current Streak") {
            Text(
                if (streakCount == 1) "1 day" else "$streakCount days"
            )
        }

        StatCard(title = "Calories Burned Today") {
            Text("$caloriesBurnedToday calories")
            Text(
                text = "Estimated from steps",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        StatCard(title = "Today vs Average") {
            Text("Today: $currentSteps steps")
            Text("Average: $averageSteps steps", modifier = Modifier.padding(top = 8.dp))

            val difference = currentSteps - averageSteps
            Text(
                text = if (difference >= 0) {
                    "${difference} above average"
                } else {
                    "${-difference} below average"
                },
                modifier = Modifier.padding(top = 8.dp)
            )

            GraphPlaceholder("Comparison graph placeholder")
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun GraphPlaceholder(
    label: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDefaults.cardColors().containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(label)
    }
}