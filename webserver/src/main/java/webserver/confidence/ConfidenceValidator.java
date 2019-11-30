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
            @Value("${validation.confidence}") double confidence,
            @Value("${validation.delay}") int delay) {

        this.attacker = new Miner(hashPower, watts, minerValue, electricityCost);
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
    private double attackerSuccessProbability(double q, int z){
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

    private double maxHashpower(double transactionValue) {
        // cost is $/s
        double kw = attacker.getWatts() * 1000;
        double ckws = attacker.getElectricityCost() / 60 / 60;
        double cost = ((kw * ckws) / attacker.getHashPower()) * delay;
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
        while(q >= 1 - confidence || z <= 6) { // TODO: remove limit of 6 once algo is finalized
            z++;
            q = attackerSuccessProbability(maxHashpower(transactionValue) / networkHashPower, z);
        }
        return z;
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
