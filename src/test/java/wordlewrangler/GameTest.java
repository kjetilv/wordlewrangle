package wordlewrangler;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class GameTest {

    @Test
    void testGame() {
        Game game = new Game(Word.fromFile(Path.of("words.txt")))
            .set("TAUNT");

        Game game1 = game.tryWord("FOYER");
        System.out.println(game1);
        Game game2 = game1.tryWord("PAINT");
        System.out.println(game2);
        Game game3 = game2.tryWord(game2.candidates().getFirst());
        assertThat(game3.done()).isTrue();
    }
}
