package com.example.testapp101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testapp101.ui.theme.Testapp101Theme
import java.util.UUID

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var isEditing: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Testapp101Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoList(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoList(modifier: Modifier = Modifier) {
    val todoList = remember { mutableStateListOf<TodoItem>() }
    var newTodoText by remember { mutableStateOf(   "") }

    Column(modifier = modifier) {
        Row {
            TextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                label = { Text("Add a new task") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (newTodoText.isNotBlank()) {
                    todoList.add(TodoItem(text = newTodoText))
                    newTodoText = ""
                }
            }) {
                Text("Add")
            }
        }
        LazyColumn {
            items(todoList) { todoItem ->
                TodoItemRow(
                    todoItem = todoItem,
                    onEditClicked = {
                        val index = todoList.indexOf(todoItem)
                        if (index != -1) {
                            todoList[index] = todoItem.copy(isEditing = true)
                        }
                    },
                    onSaveClicked = { editedText ->
                        val index = todoList.indexOf(todoItem)
                        if (index != -1) {
                            todoList[index] = todoItem.copy(text = editedText, isEditing = false)
                        }
                    },
                    onDeleteClicked = {
                        todoList.remove(todoItem)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItemRow(
    todoItem: TodoItem,
    onEditClicked: (TodoItem) -> Unit,
    onSaveClicked: (String) -> Unit,
    onDeleteClicked: (TodoItem) -> Unit
) {
    var editedText by remember { mutableStateOf(todoItem.text) }

    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        if (todoItem.isEditing) {
            TextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { onSaveClicked(editedText) }) {
                Text("Save")
            }
        } else {
            Text(
                text = todoItem.text,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { onEditClicked(todoItem) }) {
                Text("Edit")
            }
        }
        Button(onClick = { onDeleteClicked(todoItem) }) {
            Text("Delete")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    Testapp101Theme {
        TodoList()
    }
}
