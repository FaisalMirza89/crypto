package com.app.cryptoweb;

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
            @RequestParam("action") String action,
            @RequestParam("password") String password
    ) {
        try {
            byte[] result;
            if ("encrypt".equalsIgnoreCase(action)) {
                result = encryptionService.encrypt(file.getBytes(), password);
            } else if ("decrypt".equalsIgnoreCase(action)) {
                result = encryptionService.decrypt(file.getBytes(), password);
            } else {
                return ResponseEntity.badRequest().body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("processed_" + file.getOriginalFilename()).build());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<>(result, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
