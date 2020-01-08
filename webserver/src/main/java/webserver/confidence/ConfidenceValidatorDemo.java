package webserver.confidence;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class ConfidenceValidatorDemo {

    double H; // Machine TH/s
    double E; // Electricity cost/s
    double A; // Machine value amortized per second
    double D = 0.0912; // $ cost for 1 KW/s
    double T = 92000000; // Network total TH
    double O = 0.95; // Confidence Level
    int DELAY = 60; // Minimum delay between transactions in seconds

    void init(double machineTHs, double consumption, double value, int lifespan) {
        H = machineTHs;
        E = electricityCostPerSecond(consumption, D);
        A = valueAmortizedPerSecond(value, lifespan);
    }

    int confirmationsNeeded(double v) {
        double q = 1.0;
        int z = 0; // z : number of confirmations
        while(q >= 1 - O) {
            z++;
            q = attackerSuccessProbability(maxHashpower(v) / T, z);
        }
        return z;
    }

    double attackerSuccessProbability(double q, int z){
        double p = 1.0 - q;
        double lambda = z * (q / p);
        double sum = 1.0;
        int i, k;
        for (k = 0; k <= z; k++)    {
            double poisson = exp(-lambda);
            for (i = 1; i <= k; i++)
                poisson *= lambda / i;
            sum -= poisson * (1 - pow(q / p, z - k));
        }
        return sum;
    }

    double maxHashpower(double v) {
        // C is $/s
        double C = ((E + A) / H) * DELAY;
        //return (v * (1 - O)) / C;
        return (v * (1 - O)) / C;
    }

    double valueAmortizedPerSecond(double value, int lifespanMonths) {
        return value / lifespanMonths / 30 / 24 / 60 / 60;
    }
    double electricityCostPerSecond(double consumption, double costKwh) {
        // KW consumption * cost KW/s
        return (consumption / 1000) * (D / 60 / 60);
    }

    public static void main(String[] args) {
        ConfidenceValidatorDemo v = new ConfidenceValidatorDemo();

        /*int value = 10000;

        for (int i = 0; i < 6; i++) {
            String message = "Hashpower estimé: " + v.maxHashpower(value)
                    + " TH/s | probabilité réussite: " +  v
                    .attackerSuccessProbability(v.maxHashpower(value)/v.T, i);
            System.out.println(message);
        }
        System.out.println("Nombre de confirmations demandées pour une transaction d'une valeur de " + value + " $ : " +
                v.confirmationsNeeded(value) + " minimum");

         */
        /*
        for (int value = 100; value <= 70000; value += 100) {
            v.init(73, 2920, 2019, 36);
            System.out.println("" + value + "\t" +  v.confirmationsNeeded(value));
        }
        */

        for (int value = 100; value <= 70000; value += 100) {
            v.init(73, 2920, 2019, 36);
            System.out.println("" + value + "\t" +  v.maxHashpower(value));
        }

    }
}
