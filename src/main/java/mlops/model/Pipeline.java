package mlops.model;

import java.util.List;

public record Pipeline(String id, List<Stage> stages) implements PipelineElement {

    public Pipeline {
        Checks.requireText(id, "id");
        stages = List.copyOf(stages);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPipeline(this);
    }
}
