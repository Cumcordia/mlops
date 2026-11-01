package visitor;

import static testsupport.TestEvents.T0;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.Stage;
import mlops.model.Transform;
import mlops.visitor.JsonExportVisitor;

import org.junit.jupiter.api.Test;

class JsonExportVisitorTest {

    private final JsonExportVisitor visitor = new JsonExportVisitor();

    @Test
    void exportsNestedPipelineStageAndAllLeafTypes() {
        Pipeline pipeline = new Pipeline("p", List.of(new Stage("s", List.of(
                new Dataset("d", 1.5),
                new Transform("t", 0.5, List.of("d")),
                new Model("m", "v1", 2.0, T0, List.of("t")),
                new Evaluator("e", 1.0, List.of("m"))))));

        String expected = """
                {"type":"pipeline","id":"p","stages":[{"type":"stage","id":"s","members":[\
                {"type":"dataset","id":"d","sizeGb":1.5},\
                {"type":"transform","id":"t","cpuHours":0.5,"inputs":["d"]},\
                {"type":"model","id":"m","version":"v1","gpuHours":2.0,"lastTrainedAt":"2026-01-01T00:00:00Z","inputs":["t"]},\
                {"type":"evaluator","id":"e","cpuHours":1.0,"inputs":["m"]}]}]}""";

        assertEquals(expected, pipeline.accept(visitor));
    }

    @Test
    void exportsEmptyContainersAsEmptyArrays() {
        assertEquals("{\"type\":\"pipeline\",\"id\":\"p\",\"stages\":[]}",
                new Pipeline("p", List.of()).accept(visitor));
        assertEquals("{\"type\":\"stage\",\"id\":\"s\",\"members\":[]}",
                new Stage("s", List.of()).accept(visitor));
    }

    @Test
    void escapesQuotesBackslashesAndControlCharacters() {
        String json = new Dataset("a\"b\\c\nd\u0001", 1.0).accept(visitor);

        assertEquals("{\"type\":\"dataset\",\"id\":\"a\\\"b\\\\c\\nd\\u0001\",\"sizeGb\":1.0}", json);
    }
}
