package webserver.controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webserver.model.api.AddressResponse;
import webserver.service.BitcoinService;

@RestController
@RequestMapping("/bitcoin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BitcoinController {

    @Autowired
    private BitcoinService bitcoinService;

    @GetMapping("/address")
    public ResponseEntity<AddressResponse> freshAddress() {
        AddressResponse response = new AddressResponse();
        response.setAddress(bitcoinService.getReceiveAddress());
        return new ResponseEntity<AddressResponse>(response, HttpStatus.OK);
    }

    @GetMapping("/balance")
    public ResponseEntity<String> balance() {
        String response = bitcoinService.getKit().wallet().getBalance().toPlainString();
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }

    @GetMapping("/pending")
    public ResponseEntity<String> pending() {
        String response = bitcoinService.getPendingTransaction("").toString();
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }
}
