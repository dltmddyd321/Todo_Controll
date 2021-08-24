package com.example.todo_1.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.todo_1.entities.Note;

import java.util.List;

//DB를 위한 Dao 클래스 선언
@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);
    //이전 데이터를 새로운 데이터 값으로 갱신 Insert

    @Delete
    void deleteNote(Note note);

}
