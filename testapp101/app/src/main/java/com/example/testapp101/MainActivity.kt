package com.example.testapp101

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.testapp101.ui.theme.Testapp101Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Testapp101Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoList(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/** Simple in-memory model */
data class TodoItem(
    val id: Long = System.nanoTime(),
    val text: String,
    val isEditing: Boolean = false
)

@Composable
fun TodoList(modifier: Modifier = Modifier) {
    val todoList = remember { mutableStateListOf<TodoItem>() }
    var newText by remember { mutableStateOf(TextFieldValue("")) }

    Column(modifier.padding(16.dp)) {
        // Input row
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = newText,
                onValueChange = { newText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add a task…") }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                val t = newText.text.trim()
                if (t.isNotEmpty()) {
                    todoList.add(TodoItem(text = t))
                    newText = TextFieldValue("")
                }
            }) { Text("Add") }
        }

        // List
        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
            items(todoList, key = { it.id }) { item ->
                TodoItemRow(
                    todoItem = item,
                    onEditClicked = { ti ->
                        val idx = todoList.indexOfFirst { it.id == ti.id }
                        if (idx != -1) todoList[idx] = todoList[idx].copy(isEditing = true)
                    },
                    onSaveClicked = { newVal ->
                        val idx = todoList.indexOfFirst { it.id == item.id }
                        if (idx != -1) todoList[idx] = todoList[idx].copy(text = newVal, isEditing = false)
                    },
                    onDeleteClicked = { ti ->
                        // Works on API 21+
                        todoList.removeAll { it.id == ti.id }
                    }
                )
            }
        }

        // --- Lab add-ons ---
        NetworkSection()   // Task 1.2: networking (no runtime permission)
        ContactsSection()  // Task 1.3: dangerous permission demo (runtime prompt on Android 6+)
    }
}

@Composable
fun TodoItemRow(
    todoItem: TodoItem,
    onEditClicked: (TodoItem) -> Unit,
    onSaveClicked: (String) -> Unit,
    onDeleteClicked: (TodoItem) -> Unit
) {
    val ctx = LocalContext.current
    var editedText by remember(todoItem.id) { mutableStateOf(todoItem.text) }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (todoItem.isEditing) {
            TextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onSaveClicked(editedText) }) { Text("Save") }
        } else {
            Text(text = todoItem.text, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onEditClicked(todoItem) }) { Text("Edit") }
        }

        Spacer(Modifier.width(8.dp))
        // Share (IPC via implicit intent)
        Button(onClick = {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, todoItem.text)
            }
            ctx.startActivity(Intent.createChooser(send, "Share via"))
        }) { Text("Share") }

        Spacer(Modifier.width(8.dp))
        Button(onClick = { onDeleteClicked(todoItem) }) { Text("Delete") }
    }
}

/* -------------------- Network (no permission) -------------------- */
@Composable
fun NetworkSection() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("—") }
    val client = remember { OkHttpClient() }

    Column(Modifier.padding(top = 16.dp)) {
        Text("Network demo")
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val text = try {
                        val req = Request.Builder().url("https://httpbin.org/get").build()
                        client.newCall(req).execute().use { resp ->
                            "HTTP ${resp.code} | " + (resp.body?.string()?.take(120) ?: "")
                        }
                    } catch (e: Exception) {
                        "Error: ${e.message}"
                    }
                    withContext(Dispatchers.Main) { result = text }
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) { Text("Fetch") }

        Text(result, Modifier.padding(top = 8.dp))
    }
}

/* -------------------- Contacts (dangerous permission) -------------------- */
/**
 * Android 5 (API 21–22): no runtime dialog (install-time grant).
 * Android 6+ (API 23+): first tap shows a runtime permission prompt.
 */
@Composable
fun ContactsSection() {
    val ctx = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    // Ask for READ_CONTACTS at runtime (Android 6+)
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        Toast.makeText(
            ctx,
            if (granted) "Contacts permission granted" else "Permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Contact picker — we'll only launch it *after* permission is granted
    val pickContactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { pickedUri ->
        if (pickedUri == null) {
            Toast.makeText(ctx, "No contact selected", Toast.LENGTH_SHORT).show()
        } else {
            // (Optional) read/display the chosen contact’s name
            val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            ctx.contentResolver.query(pickedUri, projection, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                if (c.moveToFirst()) {
                    val name = c.getString(idx) ?: "(no name)"
                    Toast.makeText(ctx, "Picked: $name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(Modifier.padding(top = 16.dp)) {
        Text("Contacts demo")

        Button(
            onClick = {
                if (!hasPermission) {
                    // First step: request permission
                    permLauncher.launch(Manifest.permission.READ_CONTACTS)
                } else {
                    // After permission is granted: open the picker
                    pickContactLauncher.launch(null)
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(if (hasPermission) "Pick a Contact" else "Request Contacts")
        }
    }
}
