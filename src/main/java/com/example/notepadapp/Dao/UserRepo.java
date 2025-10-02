package com.example.notepadapp.Dao;

import com.example.notepadapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByResetToken(String resetToken);

    boolean existsByMobileNumber(String mobileNumber);
}
