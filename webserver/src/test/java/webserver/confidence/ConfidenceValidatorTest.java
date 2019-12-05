package webserver.confidence;

import org.junit.Before;
import org.junit.Test;

public class ConfidenceValidatorTest {

    ConfidenceValidator confidenceValidator;

    @Before
    public void setUp() {
        this.confidenceValidator = new ConfidenceValidator(73,2920, 2019,
                0.0938, 92000000 ,36, 0.95, 60);
    }

    @Test
    public void testConfirmationNeeded() {
        double value = 1000000;
        for (int i = 0; i < 6; i++) {
            System.out.println("Hashpower estimé: " + confidenceValidator.maxHashpower(value)
                    + " TH/s | probabilité réussite: " +  confidenceValidator
                        .attackerSuccessProbability(confidenceValidator.maxHashpower(value)/92000000, i));
        }
        System.out.println("Nombre de confirmations demandées pour une transaction d'une valeur de " + value + " $ : " +
                confidenceValidator.confirmationsNeeded(value) + " minimum");
    }
}
