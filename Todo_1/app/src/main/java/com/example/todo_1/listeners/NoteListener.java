package com.example.todo_1.listeners;

import com.example.todo_1.entities.Note;

public interface NoteListener {
    void onNoteClicked(Note note, int position);
}
