package com.example.healthassistant.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthassistant.data.notes.Note

@Composable
fun NoteScreen(noteViewModel: NoteViewModel = hiltViewModel()) {
    val notes = noteViewModel.noteList.collectAsState().value
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = "Experiences")
            }
        )
    }) {
        Column(modifier = Modifier.padding(6.dp)) {
            Input(onAddNote = noteViewModel::addNote)
            Divider(modifier = Modifier.padding(10.dp))
            LazyColumn {
                items(notes) { note ->
                    Text(text = note.title)
                }
            }
        }
    }
}

@Composable
fun Input(onAddNote: (Note) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val focusManager = LocalFocusManager.current
        var title by remember {
            mutableStateOf("")
        }
        val context = LocalContext.current
        TextField(
            value = title,
            onValueChange = {
                if (it.all { char ->
                        char.isLetter() || char.isWhitespace()
                    }) title = it
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            ),
            maxLines = 1,
            label = { Text(text = "Title") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )
        Button(
            onClick = {
                if (title.isNotEmpty()) {
                    onAddNote(
                        Note(title = title)
                    )
                    title = ""
                    Toast.makeText(
                        context, "Note Added",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            shape = CircleShape,
        ) {
            Text("Add Note")

        }
    }
}