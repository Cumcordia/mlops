package mlops.app;

public record MonitoringConfig(double driftAlertThreshold, double retrainMinAccuracy,
                               double retrainMaxDrift, double healthyStageAccuracy) {

    public static MonitoringConfig defaults() {
        return new MonitoringConfig(0.20, 0.80, 0.25, 0.85);
    }
}
