package webserver.confidence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

@Component
public class ConfidenceValidator {

    private Miner attacker;
    private double networkHashPower; // Network total TH
    private double confidence; // Confidence Level
    private int delay; // Minimum delay between transactions in seconds

    public ConfidenceValidator(
            @Value("${miner.hashpower}") float hashPower,
            @Value("${miner.watts}") float watts,
            @Value("${miner.value}") float minerValue,
            @Value("${miner.costkwh}")double electricityCost,
            @Value("${miner.network.hashpower}") double networkHashPower,
            @Value("${miner.lifespan}") int lifespan,
            @Value("${validation.confidence}") double confidence,
            @Value("${validation.delay}") int delay) {

        this.attacker = new Miner(hashPower, watts, minerValue, electricityCost, lifespan);
        this.networkHashPower = networkHashPower;
        this.confidence = confidence;
        this.delay = delay;

    }

    /**
     * Satoshi's formula to establish the probability of successful Double Spending
     * @param q Proportion of the attacker hashpower VS the whole network hashpower
     * @param z A number of confirmations
     * @return
     */
    public double attackerSuccessProbability(double q, int z){
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

    public double maxHashpower(double transactionValue) {
        // cost is $/s
        double kw = attacker.getWatts() / 1000;
        double ckws = attacker.getElectricityCost() / 60 / 60;
        double amortization = amortizedValue(attacker.getValue(), attacker.getLifespan());
        double cost = ( ((kw * ckws) + amortization) / attacker.getHashPower()) * delay;
        return (transactionValue * (1 - confidence)) / cost;
    }

    /**
     * Establish the safe number of confirmations needed for a given transaction value
     * @param transactionValue
     * @return
     */
    public int confirmationsNeeded(double transactionValue) {
        double q = 1.0;
        int z = 0;
        while(q >= 1 - confidence) {
            z++;
            q = attackerSuccessProbability(maxHashpower(transactionValue) / networkHashPower, z);
            if (q < 0) {
                // Out of range means the attacker would control the whole network
                // therefore human intervention is required to process the payment
                return -1;
            }
            if (z > 100) {
                break;
            }
        }
        return z;
    }

    /**
     * Amortize value to a cost per second. Value is divided by lifespan in months, per 30 days,
     * per 24 hours, per 60 minutes and then per 60 seconds.
     * @param value
     * @param lifespanMonths
     * @return amortized value/s
     */
    public double amortizedValue(double value, int lifespanMonths) {
        return value / lifespanMonths / 30 / 24 / 60 / 60;
    }

    public Miner getAttacker() {
        return attacker;
    }

    public void setAttacker(Miner attacker) {
        this.attacker = attacker;
    }

    public double getNetworkHashPower() {
        return networkHashPower;
    }

    public void setNetworkHashPower(double networkHashPower) {
        this.networkHashPower = networkHashPower;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
