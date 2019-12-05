package webserver.controler;

import webserver.model.Payment;
import webserver.model.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webserver.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("")
    public ResponseEntity<Payment> process(@RequestBody Payment payment) {
        paymentService.receive(payment);
        return new ResponseEntity<Payment>(payment, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> find(@PathVariable String id){
        Payment response = paymentService.getPaymentById(id);
        return new ResponseEntity<Payment>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<PaymentStatus> status(@PathVariable String id){
        PaymentStatus response = paymentService.paymentStatus(id);
        return new ResponseEntity<PaymentStatus>(response, HttpStatus.OK);
    }

}
