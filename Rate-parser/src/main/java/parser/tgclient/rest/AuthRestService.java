package parser.tgclient.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import parser.tgclient.auth.Auth;

@RestController
public class AuthRestService {
    @Autowired
    Auth authenticator;

    @PutMapping("/authorization")
    public void putSecurityCode(@RequestParam("code") String code) {
        authenticator.authorize(code);
    }

    @PutMapping("/password")
    public void putPassword(@RequestParam("password") String password) {
        authenticator.enterPassword(password);
    }
}
