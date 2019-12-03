package webserver.confidence;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class ConfidenceValidatorDemo {

    double H; // Machine TH/s
    double W; // Machine KW consumption
    double D; // $ cost for 1 KW/s
    double T; // Network total TH
    double O = 0.95; // Confidence Level
    int DELAY = 60; // Minimum delay between transactions in seconds

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
        double C = (((W * D) + amortizedValue(3000, 36)) / H) * DELAY;
        return (v * (1 - O)) / C;
    }

    int confirmationsNeeded(double v) {
        double q = 1.0;
        int z = 0;
        while(q >= 1 - O) {
            z++;
            q = attackerSuccessProbability(maxHashpower(v) / T, z);
        }
        return z;
    }

    public double amortizedValue(double value, int lifespanMonths) {
        return value / lifespanMonths / 30 / 24 / 60 / 60;
    }

    public static void main(String[] args) {
        ConfidenceValidatorDemo v = new ConfidenceValidatorDemo();
        v.H = 14;
        v.D = (0.0938 / 60) / 60;
        v.W = 1.35;
        v.T = 92000000;
        int value = 20000;

        for (int i = 0; i < 6; i++) {
            System.out.println(v.attackerSuccessProbability(v.maxHashpower(value)/v.T, i));
        }
        System.out.println("Nombre de confirmations demandées pour une transaction d'une valeur de " + value + " $ : " +
                v.confirmationsNeeded(value) + " minimum");

    }
}
