package com.trithuc.controller;

import com.trithuc.config.JWTTokenUtil;
import com.trithuc.model.User;
import com.trithuc.request.InfoUserRequest;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JWTTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            String result = userService.registerUser(user);
            if (result.equals("failed")) {
                return ResponseEntity.ok("User Already exist");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> loginData) {
        return userService.loginUser(loginData);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(name="Authorization") String token){
        return userService.GetProfile(token,null);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        return userService.GetProfile(null,userId);
    }

    @PostMapping("/profile/update")
    public String updateInfoUser(@RequestHeader(name="Authorization") String token, @RequestBody InfoUserRequest infoUserRequest){
        return userService.updateInfoUser(token,infoUserRequest);
    }

}
