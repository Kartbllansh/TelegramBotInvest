package org.example.controller;
import lombok.extern.log4j.Log4j;
import org.example.service.UserActivationService;
import org.example.utils.CryptoTool;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
@RequestMapping("/user")
@RestController
@Log4j
public class ActivationController {
    private final UserActivationService userActivationService;


    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }
        //TODO доработать различные ошибки
    @RequestMapping(method = RequestMethod.GET, value = "/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String id) {
        var res = userActivationService.activation(id);
        if (res) {

            userActivationService.getMessageAboutRegist(id);
            log.info("Успешная регистрация пользователя");
            return ResponseEntity.ok().body("Регистрация успешно завершена!");
        }
        return ResponseEntity.internalServerError().build();
    }
}
