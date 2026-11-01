package mlops.model;

public record Dataset(String id, double sizeGb) implements StageMember {

    public Dataset {
        Checks.requireText(id, "id");
        Checks.requireNonNegative(sizeGb, "sizeGb");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDataset(this);
    }
}
