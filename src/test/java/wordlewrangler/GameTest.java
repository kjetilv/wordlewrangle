package wordlewrangler;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

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
//        System.out.println(slate.hotCandidates());
        Game mourn = slate.tried("MOURN", "PFPPU");
//        System.out.println(mourn.hotCandidates());
        Game done = mourn.tried("FORUM", "PPPPP");
        assertThat(done.done()).isTrue();
    }

    @Test
    void test2025_10_17() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));

        Game slate = game.tried("SLATE", "PUUUU");
//        System.out.println(slate.hotCandidates());
        Game mushy = slate.tried("MUSHY", "UUPUU");
//        System.out.println(mushy.hotCandidates());
        Game crisp = mushy.tried("CRISP", "UFUFU");
//        System.out.println(crisp.hotCandidates());
        Game done = crisp.tried("GROSS", "PPPPP");
        assertThat(done.done()).isTrue();
    }
}
