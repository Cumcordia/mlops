package model;

import static testsupport.TestEvents.T0;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import mlops.model.*;

class ElementsTest {

    @Test
    void rejectsBlankIds() {
        assertThrows(IllegalArgumentException.class, () -> new Dataset(" ", 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Pipeline("", List.of()));
    }

    @Test
    void rejectsNegativeOrNonFiniteAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new Dataset("d", -1.0));
        assertThrows(IllegalArgumentException.class, () -> new Transform("t", Double.NaN, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Model("m", "v1", Double.POSITIVE_INFINITY, T0, List.of()));
    }

    @Test
    void containersCopyTheirChildrenSoTheTreeStaysStable() {
        List<StageMember> members = new ArrayList<>(List.of(new Dataset("d", 1.0)));
        Stage stage = new Stage("s", members);

        members.add(new Dataset("late", 2.0));

        assertEquals(1, stage.members().size());
        assertThrows(UnsupportedOperationException.class, () -> stage.members().add(new Dataset("x", 1.0)));
    }
}
