package wordlewrangler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintsTest {

    @Test
    public void testParse1() {
        var parse = Constraints.parse(new Word("GOOBR"), "FUUUP");
        assertThat(parse).containsExactlyInAnyOrder(
            new Constraint.Green('G', 0),
            new Constraint.Grey('O'),
            new Constraint.Grey('B'),
            new Constraint.Yellow('R', 4)
        );
    }

    @Test
    public void testParse2() {
        var parse = Constraints.parse(new Word("ABCDE"), "UUUUU");
        assertThat(parse).containsExactlyInAnyOrder(
            new Constraint.Grey('A'),
            new Constraint.Grey('B'),
            new Constraint.Grey('C'),
            new Constraint.Grey('D'),
            new Constraint.Grey('E')
        );
    }

    @Test
    public void testParse3() {
        var parse = Constraints.parse(new Word("FATAL"), "UPUUP");
        assertThat(parse).containsExactlyInAnyOrder(
            new Constraint.Grey('F'),
            new Constraint.Yellow('A', 1, 3),
            new Constraint.Grey('T'),
            new Constraint.Yellow('L', 4)
        );
    }
}
