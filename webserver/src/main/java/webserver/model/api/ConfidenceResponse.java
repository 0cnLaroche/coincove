package webserver.model.api;

import java.time.LocalDateTime;

public class ConfidenceResponse {
    private int minDepth;
    private double forValue;
    private LocalDateTime time;

    public int getMinDepth() {
        return minDepth;
    }

    public void setMinDepth(int minDepth) {
        this.minDepth = minDepth;
    }

    public double getForValue() {
        return forValue;
    }

    public void setForValue(double forValue) {
        this.forValue = forValue;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
