package com.trithuc.service.impl;

import com.trithuc.config.JWTTokenUtil;
import com.trithuc.dto.*;
import com.trithuc.model.*;
import com.trithuc.repository.DestinationRepository;
import com.trithuc.repository.PostRepository;
import com.trithuc.repository.TourRepository;
import com.trithuc.response.AuthenticationResponse;
import com.trithuc.response.EntityResponse;
import com.trithuc.repository.UserRepository;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JWTTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private PostRepository postRepository;


    @Override
    public String registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "failed";
        }
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    @Override
    public ResponseEntity<Object> loginUser(Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing username or password");
        }

        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
        Role role = user.getRole();
        String roleName = role.name();
        String token = jwtTokenUtil.generateToken(username,roleName);


        return EntityResponse.genarateResponse("Authentication", HttpStatus.OK, new AuthenticationResponse(token, role));
    }


    public User getUserFromTokenOrId(String token, Long userId) {
        if (token != null && !token.isEmpty()) {
            return userRepository.findByUsername(Authentication(token));
        } else if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null;
    }

    @Override
    public ResponseEntity<?> GetProfile(String token , Long userId) {
        try {
            User user = getUserFromTokenOrId(token, userId);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
//            Role role = Role.valueOf(roleStr.toUpperCase())
            Role role = user.getRole();
            switch (role) {
                case USER:
                    // Lấy dữ liệu profile cho user
                    ProfileDto userProfile = new ProfileDto(
                            user.getId(),
                            user.getUsername(),
                            user.getFirstname(),
                            user.getLastname(),
                            user.getEmail(),
                            user.getProfileImage(),
                            user.getAddress());
                    return ResponseEntity.ok(userProfile);

                case BUSINESS:
                    // Lấy dữ liệu profile cho business
//                    List<TourDto> tourDtoList = travelContentService.getTourByUser(userId);
//                    List<DestinationDto> destinationDtoList = travelContentService.getDestinationByUser(userId);
//                    List<PostDto> postDtoList = travelContentService.convertListPost(postRepository.findPostsByUserId(userId));
//                    BusinessProfile businessProfile = new BusinessProfile();
//                    businessProfile.setUserId(user.getId());
//                    businessProfile.setUsername(user.getUsername());
//                    businessProfile.setFirstname(user.getFirstname());
//                    businessProfile.setLastname(user.getLastname());
//                    businessProfile.setEmail(user.getEmail());
//                    businessProfile.setProfileImage(user.getProfileImage());
//                    businessProfile.setAddress(user.getAddress());
//                    businessProfile.setToursDto(tourDtoList);
//                    businessProfile.setDestinationsDto(destinationDtoList);
//                    .setPostsDto(postDtoList);
                    ProfileDto businessProfile = new ProfileDto(
                            user.getId(),
                            user.getUsername(),
                            user.getFirstname(),
                            user.getLastname(),
                            user.getEmail(),
                            user.getProfileImage(),
                            user.getAddress());
                    return ResponseEntity.ok(businessProfile);

                case ADMIN:
                    //xử lý khác biệt cho admin
                    return ResponseEntity.ok("Admin profile data");

                default:
                    return ResponseEntity.badRequest().body("Invalid role");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role value");
        }
    }

    @Override
    public String Authentication(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtTokenUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return username;
        } else {
            return null;
        }
    }

}

