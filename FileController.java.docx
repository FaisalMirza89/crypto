package com.app.controller;

import com.app.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private EncryptionService encryptionService;

    @PostMapping("/process")
    public ResponseEntity<byte[]> handleFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("action") String action
    ) {
        try {
            byte[] result;
            if ("encrypt".equalsIgnoreCase(action)) {
                result = encryptionService.encrypt(file.getBytes());
            } else if ("decrypt".equalsIgnoreCase(action)) {
                result = encryptionService.decrypt(file.getBytes());
            } else {
                return ResponseEntity.badRequest().body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename("processed_" + file.getOriginalFilename()).build());
            return new ResponseEntity<>(result, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
