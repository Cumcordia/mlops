package mlops.model;

final class Checks {

    private Checks() {
    }

    static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    static void requireNonNegative(double value, String name) {
        if (!(value >= 0.0) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be a finite number >= 0: " + value);
        }
    }
}
