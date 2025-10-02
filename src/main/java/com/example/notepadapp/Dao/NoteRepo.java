package com.example.notepadapp.Dao;

import com.example.notepadapp.model.Note;
import com.example.notepadapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepo extends JpaRepository<Note, Long> {


    List<Note> findAllByUserAndDeletedFalse(User user);

    List<Note> findByUserAndTitleContainingIgnoreCaseAndDeletedFalse(User user, String title);

    List<Note> findByUserAndContentContainingIgnoreCaseAndDeletedFalse(User user, String content);

    Note findNoteByIdAndDeletedFalse(Long id);

    List<Note> findAllByUserAndDeletedTrue(User user);
}
