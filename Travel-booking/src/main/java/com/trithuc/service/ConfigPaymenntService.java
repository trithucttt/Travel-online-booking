package com.trithuc.service;

import com.trithuc.response.MessageResponse;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;

public interface ConfigPaymenntService {
    ResponseEntity<MessageResponse> createUrlPayment() throws UnsupportedEncodingException;
}
