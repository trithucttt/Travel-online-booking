package com.trithuc.controller;

import com.trithuc.response.MessageResponse;
import com.trithuc.service.ConfigPaymenntService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class CheckOutController {


    @Autowired
    private ConfigPaymenntService configPaymenntService;

    @PostMapping("create")
    public ResponseEntity<MessageResponse> createUrlPayment() throws UnsupportedEncodingException{
        return null;
        // return configPaymenntService.createUrlPayment();
    }



}
