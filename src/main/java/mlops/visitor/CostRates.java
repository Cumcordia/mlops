package mlops.visitor;

public record CostRates(double gpuHour, double cpuHour, double storageGbMonth) {

    public CostRates {
        if (!(gpuHour >= 0.0 && cpuHour >= 0.0 && storageGbMonth >= 0.0)) {
            throw new IllegalArgumentException("rates must be >= 0");
        }
    }
}
