package wordlewrangler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintTest {

    @Test
    public void testParse1() {
        var parse = Constraint.parse(new Word("GOOBR"), "FUUUP");
        assertThat(parse).containsExactly(
            new Constraint.Found('G', 0),
            new Constraint.Unused('O'),
            new Constraint.Unused('B'),
            new Constraint.Present('R', 4)
        );
    }

    @Test
    public void testParse2() {
        var parse = Constraint.parse(new Word("ABCDE"), "UUUUU");
        assertThat(parse).containsExactly(
            new Constraint.Unused('A'),
            new Constraint.Unused('B'),
            new Constraint.Unused('C'),
            new Constraint.Unused('D'),
            new Constraint.Unused('E')
        );
    }
}
