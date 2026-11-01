package mlops.model;

public interface Visitor<R> {

    R visitPipeline(Pipeline pipeline);
    R visitStage(Stage stage);
    R visitDataset(Dataset dataset);
    R visitModel(Model model);
    R visitTransform(Transform transform);
    R visitEvaluator(Evaluator evaluator);
}
