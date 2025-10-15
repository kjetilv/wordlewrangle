package wordlewrangler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintTest {

    @Test
    public void testParse1() {
        List<Constraint> parse = Constraint.parse(new Word("FOOBR"), "FUUUP");
        assertThat(parse).containsExactly(
            new Constraint.Fixed('F', 0),
            new Constraint.Unused('O'),
            new Constraint.Unused('B'),
            new Constraint.Present('R', 4)
        );
    }
}