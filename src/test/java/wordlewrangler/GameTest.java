package wordlewrangler;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GameTest {

    @Test
    void testGame() {
        var game = new Game(Word.fromFile(Path.of("words.txt")))
            .set("TAUNT");

        var game1 = game.tryWord("FOYER");
        System.out.println(game1);
        var game2 = game1.tryWord("PAINT");
        System.out.println(game2);
        assertThat(game2.hottestCandidates()).anyMatch(
            w -> w.word().equals(new Word("TAUNT")));
        assertThat(game2.tryWord("TAUNT").done()).isTrue();
    }

    @Test
    void test2025_10_14() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));

        Game slate = game.tried("SLATE", "UUUUU");
        List<WordElim> slateCandidates = slate.hotCandidates();
//        System.out.println(slateCandidates);
        Game mourn = slate.tried("MOURN", "PFPPU");
        List<WordElim> mournCandidates = mourn.hotCandidates();
//        System.out.println(mournCandidates);
        Game done = mourn.tried("FORUM", "PPPPP");
        assertThat(done.done()).isTrue();
    }
}
