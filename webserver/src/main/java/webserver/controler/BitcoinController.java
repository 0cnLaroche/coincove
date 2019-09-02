package webserver.controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webserver.service.BitcoinService;

@RestController
@RequestMapping("/bitcoin")
public class BitcoinController {

    @Autowired
    private BitcoinService bitcoinService;

    @GetMapping("/address")
    public ResponseEntity<String> freshAddress() {
        String response = bitcoinService.getReceiveAddress();
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }

    @GetMapping("/balance")
    public ResponseEntity<String> balance() {
        String response = bitcoinService.getKit().wallet().getBalance().toPlainString();
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }
}
