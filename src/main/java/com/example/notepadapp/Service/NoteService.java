package com.example.notepadapp.Service;

import com.example.notepadapp.Dao.NoteRepo;
import com.example.notepadapp.Dao.NotesShareRepo;
import com.example.notepadapp.Dto.NoteDto;
import com.example.notepadapp.Dto.NoteResponseDto;
import com.example.notepadapp.Exception.ResourceNotFoundException;
import com.example.notepadapp.model.Note;
import com.example.notepadapp.model.NoteShare;
import com.example.notepadapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {
    @Autowired
    private UserService userService;

    @Autowired
    private NoteRepo noteRepo;

    @Autowired
    private NotesShareRepo notesShareRepo;

    public Note createNote(NoteDto notedto) {
        User user = userService.getCurrentUser();
        Note note = new Note();
        note.setTitle(notedto.getTitle());
        note.setContent(notedto.getContent());
        note.setUser(user);

        return noteRepo.save(note);
    }

    public List<NoteResponseDto> getNotesForCurrentUser(){
        User user = userService.getCurrentUser();
        List<Note> notes = noteRepo.findAllByUserAndDeletedFalse(user);
        return notes.stream().map(note->{
            NoteResponseDto dto = new NoteResponseDto();
            dto.setId(note.getId());
            dto.setTitle(note.getTitle());
            dto.setContent(note.getContent());
            dto.setCreatedAt(note.getCreatedAt());
            dto.setUpdatedAt(note.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public Note getNotesById(Long id) throws AccessDeniedException {
        User user = userService.getCurrentUser();
        Note note = noteRepo.findNoteByIdAndDeletedFalse(id);

        if (note == null) {
            throw new ResourceNotFoundException("Note not found with id: " + id);
        }

        if (!note.getUser().getId().equals(user.getId())) {
            if(notesShareRepo.findByNoteIdAndSharedWithUser(note.getId(),user)==null){
                throw new AccessDeniedException("You do not have permission to access this note.");
            }
        }
        return note;
    }

    public Note updateNote(Long id,NoteDto notedto) throws AccessDeniedException {
        User user = userService.getCurrentUser();
        Note existingNote = noteRepo.findNoteByIdAndDeletedFalse(id);

        if (existingNote == null) {
            throw new ResourceNotFoundException("Note not found with id: " + id);
        }

        if(!existingNote.getUser().getId().equals(user.getId())){
            NoteShare noteShare = notesShareRepo.findByNoteIdAndSharedWithUser(existingNote.getId(), user);
            if(noteShare==null || !"read-write".equalsIgnoreCase(noteShare.getPermission())){
                throw new AccessDeniedException("You do not have permission to edit this note.");
            }
        }
        existingNote.setTitle(notedto.getTitle());
        existingNote.setContent(notedto.getContent());
        return noteRepo.save(existingNote);
    }

    public void deleteNote(Long id) throws AccessDeniedException {
        Note existingNote = getNotesById(id);
        existingNote.setDeleted(true);
        noteRepo.save(existingNote);
    }

    public void restoreNote(Long id) throws AccessDeniedException {
        Note existingNote = noteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        User user = userService.getCurrentUser();
        if (!existingNote.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to restore this note.");
        }

        existingNote.setDeleted(false);
        noteRepo.save(existingNote);
    }

    public List<NoteResponseDto> searchNotes(String title,String keyword){
        User user = userService.getCurrentUser();

        List <Note> notes;

        if(title!=null && !title.isEmpty()){
            notes = noteRepo.findByUserAndTitleContainingIgnoreCaseAndDeletedFalse(user,title);
        }

        else if(keyword!=null && !keyword.isEmpty()){
            notes =  noteRepo.findByUserAndContentContainingIgnoreCaseAndDeletedFalse(user,keyword);
        }

        else{
            notes = Collections.emptyList();
        }

        return notes.stream().map(note -> {
                    NoteResponseDto notesdto = new NoteResponseDto();
                    notesdto.setId(note.getId());
                    notesdto.setTitle(note.getTitle());
                    notesdto.setContent(note.getContent());
                    notesdto.setCreatedAt(note.getCreatedAt());
                    notesdto.setUpdatedAt(note.getUpdatedAt());
                    return notesdto;
                }
        ).collect(Collectors.toList());
    }

    public void shareNotes(Long noteid,String username,String permission) throws AccessDeniedException {
        Note note = getNotesById(noteid);
        User userShareTo = userService.getUserByUsername(username);

        if(userShareTo==null){
            throw new ResourceNotFoundException("User not found with username: " + username);
        }

        NoteShare noteShare = new NoteShare();
        noteShare.setNote(note);
        noteShare.setSharedWithUser(userShareTo);
        noteShare.setPermission(permission);
        notesShareRepo.save(noteShare);
    }

}
