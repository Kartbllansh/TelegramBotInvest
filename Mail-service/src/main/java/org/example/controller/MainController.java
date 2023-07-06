package org.example.controller;

import org.example.dto.MailParams;
import org.example.service.MailSenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
public class MainController {
    private final MailSenderService mailSenderService;

    public MainController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }
    //TODO сделать @ControllerAdvice
    @PostMapping("/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParams mailParams) {
        mailSenderService.send(mailParams);
        return ResponseEntity.ok().build();
    }
}
