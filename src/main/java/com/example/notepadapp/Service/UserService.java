package com.example.notepadapp.Service;

import com.example.notepadapp.Dao.UserRepo;
import com.example.notepadapp.Dto.ProfileDto;
import com.example.notepadapp.Exception.ResourceNotFoundException;
import com.example.notepadapp.Exception.UnauthorizedException;
import com.example.notepadapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    UserRepo repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUser(Long id) {
        return repo.findById(id).orElse(null);
    }

    public boolean existByUsername(String Username){
        return repo.existsByUsername(Username);
    }

    public boolean existByEmail(String email){
        return repo.existsByEmail(email);
    }

    public boolean existByMobileNumber(String mobileNumber){
        return repo.existsByMobileNumber(mobileNumber);
    }

    public User save(User user) {
        // Check if password is null before encoding
        if (user.getPassword() == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public User getUserByUsername(String username) {
        User user = repo.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user;
    }

    public User getUserByEmail(String email) {
        User user = repo.findByEmail((email));
        if(user == null){
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }
        String username = authentication.getName();
        User user = repo.findByUsername(username);
        if (user == null)
            throw new ResourceNotFoundException("User not found with username: " + username);
        return user;
    }
    public boolean changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if(passwordEncoder.matches(oldPassword,user.getPassword())){
            user.setPassword(passwordEncoder.encode(newPassword));
            repo.save(user);
            return true;
        } else {
            return false;
        }
    }

    public String createPasswordResetTokenForUser(String email) {
        User user = getUserByEmail(email);

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);

        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

        repo.save(user);
        return token;
    }


    public User validatePasswordResetToken(String token) {
        User user = repo.findByResetToken(token);

        if (user == null) {
            throw new IllegalArgumentException("Invalid reset token");
        }

        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        return user;
    }


    public boolean resetPassword(String token, String newPassword) {
        User user = validatePasswordResetToken(token);

        user.setPassword(passwordEncoder.encode(newPassword));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        repo.save(user);
        return true;
    }

    public User updatedUser(ProfileDto profileDto) {
        User user = getCurrentUser();
        if(profileDto.getMobileNumber() != null && !profileDto.getMobileNumber().isEmpty()){
            user.setMobileNumber(profileDto.getMobileNumber());
        }

        if(profileDto.getEmail() != null && !profileDto.getEmail().isEmpty()){
            user.setEmail(profileDto.getEmail());
        }

        if(profileDto.getUsername() != null && !profileDto.getUsername().isEmpty()){
            user.setUsername(profileDto.getUsername());
        }
        return repo.save(user);
    }


}
