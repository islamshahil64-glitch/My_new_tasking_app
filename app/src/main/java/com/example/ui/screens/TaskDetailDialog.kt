package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TaskEntity
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onComplete: (TaskEntity) -> Unit
) {
    var isExecuting by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isReadyToClaim by remember { mutableStateOf(task.isCompleted) }
    var selectedQuizOption by remember { mutableStateOf(-1) }

    LaunchedEffect(isExecuting) {
        if (isExecuting) {
            progress = 0f
            for (i in 1..5) {
                delay(600)
                progress = i / 5f
            }
            isReadyToClaim = true
            isExecuting = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (task.category) {
                        "Apps" -> Icons.Default.PhoneAndroid
                        "Surveys" -> Icons.Default.Quiz
                        "Video" -> Icons.Default.PlayCircle
                        else -> Icons.Default.Calculate
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = task.title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Reward: +${task.rewardCoins} Coins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Est. Time: ${task.timeMinutes} mins", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()

                if (task.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Task Already Completed! Reward Claimed.",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (task.category == "Quiz") {
                    Text(text = "Question: What is 15 + 27 * 0?", fontWeight = FontWeight.Medium)
                    val options = listOf("0", "15", "42", "27")
                    options.forEachIndexed { index, option ->
                        OutlinedButton(
                            onClick = { selectedQuizOption = index },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedQuizOption == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(text = option)
                        }
                    }
                } else {
                    if (isExecuting) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Executing task simulation...", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        }
                    } else if (!isReadyToClaim) {
                        Button(
                            onClick = { isExecuting = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Task Action")
                        }
                    }
                }

                if (isReadyToClaim && !task.isCompleted) {
                    Button(
                        onClick = {
                            onComplete(task)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Claim +${task.rewardCoins} Coins 🎉", fontWeight = FontWeight.Bold)
                    }
                }

                if (task.category == "Quiz" && selectedQuizOption != -1 && !task.isCompleted) {
                    Button(
                        onClick = {
                            if (selectedQuizOption == 1) { // Correct answer is 15
                                onComplete(task)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Answer")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
