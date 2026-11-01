package mlops.visitor;

import java.util.ArrayList;
import java.util.List;

import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.Stage;
import mlops.model.StageMember;
import mlops.model.Transform;
import mlops.model.Visitor;

public final class LineageReportVisitor implements Visitor<Void> {

    private final List<String> lines = new ArrayList<>();
    private String currentStage = "";

    @Override
    public Void visitPipeline(Pipeline pipeline) {
        for (Stage stage : pipeline.stages()) {
            stage.accept(this);
        }
        return null;
    }

    @Override
    public Void visitStage(Stage stage) {
        String enclosingStage = currentStage;
        currentStage = stage.id();
        for (StageMember member : stage.members()) {
            member.accept(this);
        }
        currentStage = enclosingStage;
        return null;
    }

    @Override
    public Void visitDataset(Dataset dataset) {
        record("dataset", dataset.id(), List.of());
        return null;
    }

    @Override
    public Void visitModel(Model model) {
        record("model", model.id(), model.inputs());
        return null;
    }

    @Override
    public Void visitTransform(Transform transform) {
        record("transform", transform.id(), transform.inputs());
        return null;
    }

    @Override
    public Void visitEvaluator(Evaluator evaluator) {
        record("evaluator", evaluator.id(), evaluator.inputs());
        return null;
    }

    public List<String> lines() {
        return List.copyOf(lines);
    }

    private void record(String kind, String id, List<String> inputs) {
        StringBuilder line = new StringBuilder();
        if (!currentStage.isEmpty()) {
            line.append('[').append(currentStage).append("] ");
        }
        line.append(kind).append(' ').append(id);
        if (!inputs.isEmpty()) {
            line.append(" <- ").append(String.join(", ", inputs));
        }
        lines.add(line.toString());
    }
}
