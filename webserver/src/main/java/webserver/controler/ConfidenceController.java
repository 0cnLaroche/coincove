package webserver.controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import webserver.confidence.ConfidenceValidator;
import webserver.model.api.ConfidenceResponse;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/confidence")
public class ConfidenceController {

    @Autowired
    private ConfidenceValidator confidenceValidator;

    @GetMapping("")
    public ResponseEntity<ConfidenceResponse> getConfidence(@RequestParam double value) {
        ConfidenceResponse response = new ConfidenceResponse();
        response.setForValue(value);
        response.setMinDepth(confidenceValidator.confirmationsNeeded(value));
        response.setTime(LocalDateTime.now());
        return new ResponseEntity<ConfidenceResponse>(response, HttpStatus.OK);
    }
}
