package com.example.notepadapp.controller;

import com.example.notepadapp.Dto.*;
import com.example.notepadapp.Service.EmailService;
import com.example.notepadapp.Service.JwtService;
import com.example.notepadapp.Service.TokenBlacklistService;
import com.example.notepadapp.Service.UserService;
import com.example.notepadapp.model.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private EmailService emailService;

    private static final Pattern Email_Pattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private static final Pattern Mobile_Pattern = Pattern.compile("^\\d{10}$");

    private static final Pattern Password_Pattern = Pattern.compile("^[A-Za-z0-9@#%^&*(){}:]{6,15}$");

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userdto) {
        // Validate required fields
        if (userdto.getUsername() == null || userdto.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        if(userService.existByUsername(userdto.getUsername())){
            return ResponseEntity.badRequest().body("Username is Already Exist . Please choose another username");
        }
        if (userdto.getPassword() == null || userdto.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (userdto.getEmail() == null || userdto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        if(userService.existByEmail(userdto.getEmail())){
            return ResponseEntity.badRequest().body("Email Already Exist.");
        }

        if (userdto.getMobileNumber() == null || userdto.getMobileNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        if(userService.existByMobileNumber(userdto.getMobileNumber())){
            return ResponseEntity.badRequest().body("Mobile Number Already Exist.");
        }

        if(!Email_Pattern.matcher(userdto.getEmail()).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        if (!Mobile_Pattern.matcher(userdto.getMobileNumber()).matches()) {
            return ResponseEntity.badRequest().body("Mobile number must be 10 digits");
        }

        if(userdto.getPassword().length()<8 && !Password_Pattern.matcher(userdto.getPassword()).matches()) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
        }

        if (userService.existByUsername(userdto.getUsername())) {
            return ResponseEntity.badRequest().body("Username is Already Exist");
        }

        if (userService.existByEmail(userdto.getEmail())) {
            return ResponseEntity.badRequest().body("Email is Already Exist");
        }

        User newUser = new User();
        newUser.setUsername(userdto.getUsername());
        newUser.setEmail(userdto.getEmail());
        newUser.setPassword(userdto.getPassword());
        newUser.setMobileNumber(userdto.getMobileNumber());

        String roles = "USER";
        newUser.setRole(roles);

        try {
            User savedUser = userService.save(newUser);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginDto logindto) {
        // Validate required fields
        if (logindto.getUsername() == null || logindto.getUsername().trim().isEmpty()) {
            return new ResponseEntity<>("Username is required", HttpStatus.BAD_REQUEST);
        }
        if (logindto.getPassword() == null || logindto.getPassword().trim().isEmpty()) {
            return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(logindto.getUsername(), logindto.getPassword()));
            if (authentication.isAuthenticated()) {
                User user = userService.getUserByUsername(logindto.getUsername());

                return new ResponseEntity<>(jwtService.generateToken(user), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Authorization Failed", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Authorization Failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claims = jwtService.extractAllClaims(token);
            if (claims == null) {
                return new ResponseEntity<>("Invalid token.", HttpStatus.BAD_REQUEST);
            }
            Date expiryDate = claims.getExpiration();

            tokenBlacklistService.blacklistToken(token, expiryDate);

            return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("No Authorization token found", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        if (request.email == null || request.email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            // This correctly calls the logic in your UserService
            String token = userService.createPasswordResetTokenForUser(request.email);

            // This link now points to your backend's verification endpoint
            String resetLink = "http://localhost:8081/api/auth/reset-password?token=" + token;

            emailService.sendPasswordResetEmail(request.email, resetLink);

            return ResponseEntity.ok("If an account with this email exists, a password reset link has been sent.");


        } catch (UsernameNotFoundException e) {
            // For security, do not reveal if the user was not found.
            // Return a generic success message to prevent user enumeration.
            return ResponseEntity.ok("If an account with this email exists, a password reset link has been generated.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        if (request.token == null || request.newPassword == null) {
            return ResponseEntity.badRequest().body("Token and new password are required.");
        }

        if(!Password_Pattern.matcher(request.newPassword).matches()) {
            return ResponseEntity.badRequest().body("Password must be 6 to 15 characters long and can include letters, numbers, and special characters @#%^&*(){}:");
        }

        try {
            // Use the existing reset password logic
            boolean success = userService.resetPassword(request.token, request.newPassword);
            if (success) {
                return ResponseEntity.ok("Password has been reset successfully.");
            } else {
                return ResponseEntity.badRequest().body("Password reset failed.");
            }
        } catch (IllegalArgumentException e) {
            // Catches invalid or expired tokens from the validation step
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData ) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if( oldPassword == null || oldPassword.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            return new ResponseEntity<>("Old password and new password both are required", HttpStatus.BAD_REQUEST);
        }

        if(newPassword.length()<8 && !Password_Pattern.matcher(newPassword).matches()) {
            return new ResponseEntity<>("Password must be 6 to 15 characters long and can include letters, numbers, and special characters @#%^&*(){}:", HttpStatus.BAD_REQUEST);
        }

        boolean isChanged = userService.changePassword(oldPassword, newPassword);
        if (isChanged) {
            return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Old password is incorrect", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        try {
            User currentUser = userService.getCurrentUser();
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileDto profileDto) {
        User user = userService.getCurrentUser();
        try{
            if(profileDto.getMobileNumber()!=null && !profileDto.getMobileNumber().isEmpty()){
                if(userService.existByMobileNumber(profileDto.getMobileNumber()) && !user.getMobileNumber().equals(profileDto.getMobileNumber())){
                    return ResponseEntity.badRequest().body("Mobile Number is Already Exist");
                }

                if (!Mobile_Pattern.matcher(profileDto.getMobileNumber()).matches()) {
                    return ResponseEntity.badRequest().body("Mobile number must be 10 digits");
                }
            }

            if(profileDto.getEmail()!=null && !profileDto.getEmail().isEmpty()){
                if(!Email_Pattern.matcher(profileDto.getEmail()).matches()) {
                    return ResponseEntity.badRequest().body("Invalid email format");
                }

                if(userService.existByEmail(profileDto.getEmail()) && !user.getEmail().equals(profileDto.getEmail())){
                    return ResponseEntity.badRequest().body("Email is Already Exist");
                }
            }

            if(profileDto.getUsername()!=null && !profileDto.getUsername().isEmpty()){
                if(userService.existByUsername(profileDto.getUsername()) && !user.getUsername().equals(profileDto.getUsername())){
                    return ResponseEntity.badRequest().body("Username is Already Exist");
                }
            }

            User updatedUser = userService.updatedUser(profileDto);
            return ResponseEntity.ok(updatedUser);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
    }


}

