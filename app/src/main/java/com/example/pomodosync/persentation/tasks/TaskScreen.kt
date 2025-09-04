package com.example.pomodosync.persentation.tasks

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pomodosync.data.local.entity.Task
import com.example.pomodosync.persentation.component.Counter



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    onTaskClick: (taskId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState() // Use 'by' delegate for cleaner access

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
//        topBar = {
//            TopAppBar(
//                title = { Text(text = "Tasks") }, colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    titleContentColor = MaterialTheme.colorScheme.onSurface
//                )
//            )
//        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                uiState.error?.let { error ->
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                TaskList(
                    tasks = uiState.tasks, onEditClick = { taskToEdit ->
                    viewModel.startEditingTask(taskToEdit)
                    showDialog = true
                }, onCompleteChange = { task, isCompleted ->
                    viewModel.updateTaskCompletion(task, isCompleted)
                }, onTaskClick = onTaskClick
                )
            }

            FloatingActionButton(
                onClick = {
                    viewModel.clearEditingState() // Ensure we are in "add new" mode
                    showDialog = true
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }

            if (showDialog) {
                TaskDialogue(
                    task = uiState.taskInput,
                    count = uiState.sessionCount,
                    isEditing = uiState.selectedTask != null, // Pass editing status to dialog
                    onValueChange = { viewModel.updateTaskInput(it) },
                    onCountChange = { viewModel.updateSessionCount(it) },
                    onConfirm = {
                        viewModel.saveTask()
                        showDialog = false
                    },
                    onDismiss = {
                        viewModel.clearEditingState() // Clear editing state on dismiss
                        showDialog = false
                    })
            }
        }
    }
}


@Composable
fun TaskComponent(
    task: Task,
    onCompleteChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onTaskClick: (taskId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {
                onTaskClick(task.taskId)
            }, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = task.isCompleted,
                onClick = { onCompleteChange(!task.isCompleted) },
            )

            Text(
                text = task.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(horizontal = 8.dp) // Add some padding around the text
                    .weight(1f),
                overflow = TextOverflow.Ellipsis
            )

            IconButton(
                onClick = onEditClick
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Button",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onCompleteChange: (Task, Boolean) -> Unit,
    onEditClick: (Task) -> Unit,
    onTaskClick: (taskId: Long) -> Unit,
) {
    if (tasks.isEmpty()) {
        Text(
            "No tasks yet. Add one!",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp) // Add some padding around the list
        ) {
            items(items = tasks, key = { task -> task.taskId }) { task -> // Use task.id as key
                TaskComponent(
                    task = task, // Pass the whole task object
                    onCompleteChange = { isCompleted -> onCompleteChange(task, isCompleted) },
                    onEditClick = { onEditClick(task) },
                    onTaskClick = onTaskClick
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialogue(
    task: String,
    count: Int,
    isEditing: Boolean, // To change dialog title or button text if needed
    onValueChange: (String) -> Unit,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // Increased padding inside the card
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditing) "Edit Task" else "Add New Task", // Dynamic title
                    modifier = Modifier.padding(bottom = 16.dp), // Spacing after title
                    style = MaterialTheme.typography.headlineSmall, // Adjusted style
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = task,
                    onValueChange = onValueChange,
                    // colors = ... (keep your colors)
                    label = { Text("Task Name"/*, color = ...*/) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp) // Spacing
                )
                Text(
                    text = "Expected Pomodoros",
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start),
                    style = MaterialTheme.typography.titleSmall, // Adjusted style
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Counter(
                    count = count,
                    onCountChange = onCountChange,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 20.dp) // Spacing
                )
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Dismiss")
                    }
                    Button(onClick = onConfirm) {
                        Text(if (isEditing) "Save Changes" else "Add Task")
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showSystemUi = true, showBackground = true)
fun TaskScreenPreview() {
    TaskScreen(
        onTaskClick = { })
}
