package mlops.model;

import java.util.List;

public record Stage(String id, List<StageMember> members) implements PipelineElement {

    public Stage {
        Checks.requireText(id, "id");
        members = List.copyOf(members);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitStage(this);
    }
}
