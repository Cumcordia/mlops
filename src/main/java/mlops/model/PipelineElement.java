package mlops.model;

public interface PipelineElement {

    String id();
    <R> R accept(Visitor<R> visitor);
}
