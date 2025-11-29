package wordlewrangler;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
    void dist() {
        Game game = new Game(Word.fromFile(Path.of("words.txt")));
        LetterDistributions distribution = game.distribution();
        distribution.distributions()
            .stream()
            .map(Object::toString)
            .forEach(System.out::println);

//        System.out.println(distribution.getFirst().score('S'));
//        System.out.println(distribution.getFirst().score('Y'));
    }

    @Test
    void test2025_10_14() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));

        Game slate = game.tried("SLATE", "UUUUU");
        System.out.println(slate.hotCandidatesDescending());
//        Game mourn = slate.tried("MOURN", "PFPPU");
//        System.out.println(mourn.hotCandidates());
//        Game done = mourn.tried("FORUM", "PPPPP");
//        assertThat(done.done()).isTrue();
    }

    @Test
    void test2025_10_17() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        Game slate = game.tried("SLATE", "UUPUP");
        System.out.println(slate.hottestCandidates());
        Game amber = slate.tried("AMBER", "PUUFU");
        System.out.println(amber.hottestCandidates());
        Game haven = slate.tried("HAVEN", "UUUUU");
//        Game mushy = slate.tried("HEARD", "UFFFF");
//        System.out.println(mushy.hotCandidates());
//        Game crisp = mushy.tried("CRISP", "UFUFU");
//        System.out.println(crisp.hotCandidates());
//        Game done = crisp.tried("GROSS", "PPPPP");
//        assertThat(done.done()).isTrue();
    }

    @Test
    void test2025_10_20() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        Game slate = game.tried("SLATE", "UPUUU");
        System.out.println(slate.hottestCandidates());
        Game lurid = slate.tried("LURID", "FUUPU");
        System.out.println(lurid.hottestCandidates());
        Game lingo = lurid.tried("LINGO", "FFUUF");
        System.out.println(lingo.hottestCandidates());

//        Game mushy = slate.tried("HEARD", "UFFFF");
//        System.out.println(mushy.hotCandidates());
//        Game crisp = mushy.tried("CRISP", "UFUFU");
//        System.out.println(crisp.hotCandidates());
//        Game done = crisp.tried("GROSS", "PPPPP");
//        assertThat(done.done()).isTrue();
    }

    @Test
    void test2025_10_21() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UUUPP");
        System.out.println(slate.hottestCandidates());
        var tenor = game.tried("TENOR", "PFUFU");
        System.out.println(tenor.hottestCandidates());
        var depot = game.tried("DEPOT", "FFUFP");
        System.out.println(depot.hottestCandidates());
    }

    @Test
    void test2025_10_22() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "FUUPU");
        System.out.println(slate.hottestCandidates());
        var snout = game.tried("SNOUT", "FPUPF");
        System.out.println(snout.hottestCandidates());
//        var depot = game.tried("DEPOT", "FFUFP");
//        System.out.println(depot.hottestCandidates());
    }

    @Test
    void test2025_10_24() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UUUPP");
        System.out.println(slate.hotCandidatesDescending());
        var bunch = slate.tried("BUNCH", "PFUUU");
        System.out.println(bunch.hotCandidatesDescending());
//        var depot = game.tried("DEPOT", "FFUFP");
//        System.out.println(depot.hottestCandidates());
    }

    @Test
    void test2025_10_25() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UUPUF");
        var barge = slate.tried("BARGE", "UFUFF");
        LetterDistributions distribution = barge.distribution();
        List<WordElim> hotCandidatesDescending = barge.hotCandidatesDescending();

        System.out.println(distribution);

        hotCandidatesDescending.stream()
            .map(elim ->
                Map.entry(elim, distribution.scoreStr(elim.word())))
            .forEach(System.out::println);
//        var bunch = slate.tried("BUNCH", "");
//        System.out.println(bunch.hotCandidatesDescending());
//        var depot = game.tried("DEPOT", "FFUFP");
//        System.out.println(depot.hottestCandidates());
    }

    @Test
    void test2025_10_27() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        game = game.tried("SLATE", "UUUUU");
        game = game.tried("CRONY", "UFUUU");
//        game = game.tried("HARPY", "UFPUU");
//        game = game.tried("BLARE", "UFFFF");
//        game = game.tried("DETER", "PFFUU");
//        game = game.tried("FLUID", "UFFUU");
//        game = game.tried("BAGEL", "UPUPF");
//        game = game.tried("PEDAL", "UPPFF");
//        var barge = slate.tried("BARGE", "UFUFF");

        LetterDistributions distribution = game.distribution();
        List<WordElim> hotCandidatesDescending = game.hotCandidatesDescending();
//        List<WordElim> hotEliminatorsDescending = game.hotEliminatorsDescending();

        System.out.println(hotCandidatesDescending);
//        System.out.println(hotEliminatorsDescending);

        System.out.println(distribution);

        WordScores scores = game.wordScores();

        scores.ratings().forEach(System.out::println);

//        hotCandidatesDescending.stream()
//            .map(elim ->
//                Map.entry(elim, distribution.scoreStr(elim.word())))
//            .forEach(System.out::println);
//        var bunch = slate.tried("BUNCH", "");
//        System.out.println(bunch.hotCandidatesDescending());
//        var depot = game.tried("DEPOT", "FFUFP");
//        System.out.println(depot.hottestCandidates());
    }
}
