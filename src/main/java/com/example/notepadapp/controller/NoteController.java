package com.example.notepadapp.controller;
import com.example.notepadapp.Dao.AttachmentRepo;
import com.example.notepadapp.Dto.NoteDto;
import com.example.notepadapp.Dto.NoteResponseDto;
import com.example.notepadapp.Dto.SharedNotesRequestDto;
import com.example.notepadapp.Exception.ResourceNotFoundException;
import com.example.notepadapp.Service.NoteService;
import com.example.notepadapp.model.Attachment;
import com.example.notepadapp.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@PreAuthorize("isAuthenticated()")
public class NoteController {

    @Autowired
    private NoteService noteService;
    @Autowired
    private AttachmentRepo attachmentRepo;

    @PostMapping("/create")
    public ResponseEntity<Note> addNote(@RequestBody NoteDto notedto) {
        Note createdNote = noteService.createNote(notedto);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<NoteResponseDto>> getAllNotesForUser() {
        List<NoteResponseDto> notes = noteService.getNotesForCurrentUser();
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        try {
            Note note = noteService.getNotesById(id);
            return new ResponseEntity<>(note, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody NoteDto notedto) {
        try{
            Note updatedNote = noteService.updateNote(id, notedto);
            return new ResponseEntity<>(updatedNote, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteNote(@PathVariable Long id) {
        try {
            noteService.deleteNote(id);
            return ResponseEntity.ok("Note moved to trash.");
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>("You do not have permission to delete this note.", HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while deleting the note: " + e.getMessage(),
                                       HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteResponseDto>> searchNotes(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String keyword) {

        List<NoteResponseDto> notes = noteService.searchNotes(title, keyword);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @PostMapping("/restore/{id}")
    public ResponseEntity<String> restoreNote (@PathVariable Long id) throws AccessDeniedException{
        try{
            noteService.restoreNote(id);
            return new ResponseEntity<>("Note restored successfully.", HttpStatus.OK);
        }
        catch(AccessDeniedException e){
            return new ResponseEntity<>("You do not have permission to restore this note.", HttpStatus.FORBIDDEN);
        }
        catch(ResourceNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            return new ResponseEntity<>("An error occurred while restoring the note: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/share/{noteId}")
    public ResponseEntity<String> shareNote(@PathVariable Long noteId, @RequestBody SharedNotesRequestDto sharedNotesRequestDto) {
        try {
            noteService.shareNotes(noteId,sharedNotesRequestDto.getUsername(), sharedNotesRequestDto.getPermission());
            return new ResponseEntity<>("Note shared successfully.", HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>("You do not have permission to share this note.", HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while sharing the note: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<String> addAttachment(@PathVariable Long id,@RequestParam("file") MultipartFile file) {
        try{
            Note note = noteService.getNotesById(id);
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setSize(file.getSize());
            attachment.setData(file.getBytes());
            attachment.setNote(note);
            attachmentRepo.save(attachment);
            return new ResponseEntity<>("Attachment added successfully.", HttpStatus.OK);
        }catch(IOException e){
            return new ResponseEntity<>("Failed to read the file data: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception e){
            return new ResponseEntity<>("An error occurred while adding the attachment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
