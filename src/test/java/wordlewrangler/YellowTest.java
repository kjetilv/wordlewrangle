package wordlewrangler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YellowTest {

    @Test
    void eliminates() {
        var fooba = new Word("FOOBA");
        var yellowB = new Constraint.Yellow('B', 3);
        var yellowX = new Constraint.Yellow('X', 4);
        assertThat(yellowB.eliminates(fooba.letters())).isTrue();
        assertThat(yellowX.eliminates(fooba.letters())).isFalse();
    }

    @Test
    void containsAtSeveral() {
        var fooba = new Word("FOOBA");
        assertThat(new Constraint.Yellow('O', 2, 3).eliminates(fooba.letters())).isTrue();
        assertThat(new Constraint.Yellow('O', 1).eliminates(fooba.letters())).isTrue();
        assertThat(new Constraint.Yellow('O', 2).eliminates(fooba.letters())).isTrue();
        assertThat(new Constraint.Yellow('O', 0, 1).eliminates(fooba.letters())).isTrue();
        assertThat(new Constraint.Yellow('O', 2, 3).eliminates(fooba.letters())).isTrue();
    }

}
