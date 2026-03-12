package wordlewrangler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordTest {

    @Test
    void containsAt() {
        var fooba = new Word("FOOBA");
        assertThat(fooba.containsAt('B', new int[] {3})).isTrue();
        assertThat(fooba.containsAt('X', new int[] {4})).isFalse();
    }

    @Test
    void containsAtSeveral() {
        var fooba = new Word("FOOBA");
        assertThat(fooba.containsAt('O', new int[] {1, 2})).isTrue();
        assertThat(fooba.containsAt('O', new int[] {1})).isTrue();
        assertThat(fooba.containsAt('O', new int[] {2})).isTrue();
        assertThat(fooba.containsAt('O', new int[] {0, 1})).isTrue();
        assertThat(fooba.containsAt('O', new int[] {2, 3})).isTrue();
    }

}
