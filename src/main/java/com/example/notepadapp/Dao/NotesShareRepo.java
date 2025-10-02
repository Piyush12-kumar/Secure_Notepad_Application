package com.example.notepadapp.Dao;

import com.example.notepadapp.model.NoteShare;
import com.example.notepadapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotesShareRepo extends JpaRepository<NoteShare, Long> {
    NoteShare findByNoteIdAndSharedWithUser(Long noteId, User sharedWithUser);
}
