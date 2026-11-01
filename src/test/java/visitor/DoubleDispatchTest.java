package visitor;

import static testsupport.TestEvents.T0;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.PipelineElement;
import mlops.model.Stage;
import mlops.model.Transform;
import mlops.model.Visitor;
import org.junit.jupiter.api.Test;

class DoubleDispatchTest {

    private static final class KindVisitor implements Visitor<String> {
        @Override
        public String visitPipeline(Pipeline pipeline) {
            return "pipeline";
        }

        @Override
        public String visitStage(Stage stage) {
            return "stage";
        }

        @Override
        public String visitDataset(Dataset dataset) {
            return "dataset";
        }

        @Override
        public String visitModel(Model model) {
            return "model";
        }

        @Override
        public String visitTransform(Transform transform) {
            return "transform";
        }

        @Override
        public String visitEvaluator(Evaluator evaluator) {
            return "evaluator";
        }
    }

    @Test
    void everyElementCallsTheVisitMethodOfItsOwnType() {
        List<PipelineElement> elements = List.of(
                new Pipeline("p", List.of()),
                new Stage("s", List.of()),
                new Dataset("d", 1.0),
                new Model("m", "v1", 1.0, T0, List.of()),
                new Transform("t", 1.0, List.of()),
                new Evaluator("e", 1.0, List.of()));

        List<String> kinds = elements.stream().map(element -> element.accept(new KindVisitor())).toList();

        assertEquals(List.of("pipeline", "stage", "dataset", "model", "transform", "evaluator"), kinds);
    }
}
