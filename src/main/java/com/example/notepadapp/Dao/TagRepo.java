package com.example.notepadapp.Dao;

import com.example.notepadapp.model.NoteTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepo extends JpaRepository<NoteTags, Long> {
    NoteTags findByTagName(String tagName);
}
