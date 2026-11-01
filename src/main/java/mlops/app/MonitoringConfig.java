package mlops.app;

import java.time.Duration;
import mlops.visitor.CostRates;

public record MonitoringConfig(double driftAlertThreshold, double retrainMinAccuracy, double retrainMaxDrift,
                               double healthyStageAccuracy, CostRates costRates, Duration modelMaxAge) {

    public static MonitoringConfig defaults() {
        return new MonitoringConfig(0.20, 0.80, 0.25, 0.85, new CostRates(2.50, 0.10, 0.02), Duration.ofDays(30));
    }
}
