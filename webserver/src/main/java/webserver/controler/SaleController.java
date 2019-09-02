package webserver.controler;

import webserver.model.Sale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sale")
public class SaleController {

    @PostMapping("")
    public ResponseEntity<Sale> submit(@RequestBody Sale sale) {

        return new ResponseEntity<Sale>(sale, HttpStatus.ACCEPTED);
    }
}
