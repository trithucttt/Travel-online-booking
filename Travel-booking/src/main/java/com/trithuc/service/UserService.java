package com.trithuc.service;

import com.trithuc.model.Role;
import com.trithuc.model.User;
import com.trithuc.request.InfoUserRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface UserService {

    public String registerUser(User user);

    ResponseEntity<Object> loginUser(Map<String, String> loginData);

    public ResponseEntity<?> GetProfile(String token , Long userId) ;

    String updateInfoUser(String token, InfoUserRequest infoUserRequest);

    public String Authentication(String token);

}
