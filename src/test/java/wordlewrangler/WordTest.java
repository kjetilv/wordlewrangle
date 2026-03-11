package wordlewrangler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordTest {

    @Test
    void containsAt() {
        var fooba = new Word("FOOBA");
        assertThat(fooba.containsAt(new int[] {3}, 'B')).isTrue();
        assertThat(fooba.containsAt(new int[] {4}, 'X')).isFalse();
    }

    @Test
    void containsAtSeveral() {
        var fooba = new Word("FOOBA");
        assertThat(fooba.containsAt(new int[] {1, 2}, 'O')).isTrue();
        assertThat(fooba.containsAt(new int[] {1}, 'O')).isTrue();
        assertThat(fooba.containsAt(new int[] {2}, 'O')).isTrue();
        assertThat(fooba.containsAt(new int[] {0, 1}, 'O')).isFalse();
        assertThat(fooba.containsAt(new int[] {2, 3}, 'O')).isFalse();
    }

}
