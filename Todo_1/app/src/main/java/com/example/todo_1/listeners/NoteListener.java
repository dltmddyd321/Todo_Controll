package com.example.todo_1.listeners;

import com.example.todo_1.entities.Note;

//특정 메모 클릭 후 데이터 이용을 위한 Listener 선언
public interface NoteListener {
    void onNoteClicked(Note note, int position);
}
