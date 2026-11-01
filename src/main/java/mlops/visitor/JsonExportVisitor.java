package mlops.visitor;

import java.util.List;
import java.util.stream.Collectors;
import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.PipelineElement;
import mlops.model.Stage;
import mlops.model.Transform;
import mlops.model.Visitor;

/** Export visitor: hand-written compact JSON that mirrors the nesting of the tree. Stateless. */
public final class JsonExportVisitor implements Visitor<String> {

    @Override
    public String visitPipeline(Pipeline pipeline) {
        return object("pipeline", pipeline.id(), field("stages", array(pipeline.stages())));
    }

    @Override
    public String visitStage(Stage stage) {
        return object("stage", stage.id(), field("members", array(stage.members())));
    }

    @Override
    public String visitDataset(Dataset dataset) {
        return object("dataset", dataset.id(), field("sizeGb", number(dataset.sizeGb())));
    }

    @Override
    public String visitModel(Model model) {
        return object("model", model.id(),
                field("version", quote(model.version())),
                field("gpuHours", number(model.gpuHours())),
                field("lastTrainedAt", quote(model.lastTrainedAt().toString())),
                field("inputs", strings(model.inputs())));
    }

    @Override
    public String visitTransform(Transform transform) {
        return object("transform", transform.id(),
                field("cpuHours", number(transform.cpuHours())),
                field("inputs", strings(transform.inputs())));
    }

    @Override
    public String visitEvaluator(Evaluator evaluator) {
        return object("evaluator", evaluator.id(),
                field("cpuHours", number(evaluator.cpuHours())),
                field("inputs", strings(evaluator.inputs())));
    }

    private String array(List<? extends PipelineElement> elements) {
        return elements.stream().map(element -> element.accept(this)).collect(Collectors.joining(",", "[", "]"));
    }

    private static String strings(List<String> values) {
        return values.stream().map(JsonExportVisitor::quote).collect(Collectors.joining(",", "[", "]"));
    }

    private static String object(String type, String id, String... fields) {
        StringBuilder json = new StringBuilder("{")
                .append(field("type", quote(type))).append(',')
                .append(field("id", quote(id)));
        for (String field : fields) {
            json.append(',').append(field);
        }
        return json.append('}').toString();
    }

    private static String field(String name, String jsonValue) {
        return quote(name) + ":" + jsonValue;
    }

    private static String number(double value) {
        return Double.toString(value);
    }

    private static String quote(String text) {
        StringBuilder quoted = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> {
                    if (c < 0x20) {
                        quoted.append(String.format("\\u%04x", (int) c));
                    } else {
                        quoted.append(c);
                    }
                }
            }
        }
        return quoted.append('"').toString();
    }
}
