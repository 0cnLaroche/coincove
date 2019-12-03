package webserver.confidence;

/**
 * Represents an attacker machine. Should be built using the specs of the best machine available on the market
 */

public class Miner {

    private float hashPower; // Machine TH/s
    private float watts; // Machine KW consumption
    private float value; // Machine value in dollars
    private double electricityCost; // $ cost for 1 KW/h
    private int lifespan; // Lifespan before obsolete or fails

    /**
     *
     * @param hashPower Machine TH/s
     * @param watts Electricity consumption in watts
     * @param value Market value
     * @param electricityCost Electricity cost for one kw/h
     */

    public Miner(float hashPower, float watts, float value, double electricityCost, int lifespan) {
        this.hashPower = hashPower;
        this.watts = watts;
        this.value = value;
        this.electricityCost = electricityCost;
        this.lifespan = lifespan;
        this.value = value;
    }

    public Miner() {
    }

    /**
     * @return Machine hash power in TH/s
     */
    public float getHashPower() {
        return hashPower;
    }

    /**
     * Set the machine hash power in TH/s
     * @param hashPower
     */
    public void setHashPower(float hashPower) {
        this.hashPower = hashPower;
    }

    public float getWatts() {
        return watts;
    }

    public void setWatts(float watts) {
        this.watts = watts;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public double getElectricityCost() {
        return electricityCost;
    }

    public void setElectricityCost(double electricityCost) {
        this.electricityCost = electricityCost;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }


}
