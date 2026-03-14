package wordlewrangler;

import module java.base;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;

import static java.lang.IO.println;
import static org.assertj.core.api.Assertions.assertThat;

public class GameTest {

    @Test
    void testGame() {
        var game = new Game(Word.fromFile(Path.of("words.txt")))
            .set("TAUNT");

        var game1 = game.tryWord("FOYER");
        println(game1);
        var game2 = game1.tryWord("PAINT");
        println(game2);
        var actual = game2.hottestCandidates();
        assertThat(actual).map(WordElim::word)
            .doesNotContain(new Word("TAUNT"))
            .contains(new Word("TAWNY"));
        assertThat(game2.tryWord("TAUNT").done()).isTrue();
    }

    @Test
    void dist() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var distribution = game.distribution();
        distribution.distributions()
            .stream()
            .map(Object::toString)
            .forEach(System.out::println);

//        IO.println(distribution.getFirst().score('S'));
//        IO.println(distribution.getFirst().score('Y'));
    }

    @Test
    void test2025_10_14() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));

        var slate = game.tried("SLATE", "UUUUU");
        println(slate.hotCandidatesDescending());
//        Game mourn = slate.tried("MOURN", "PFPPU");
//        IO.println(mourn.hotCandidates());
//        Game done = mourn.tried("FORUM", "PPPPP");
//        assertThat(done.done()).isTrue();
    }

    @Test
    void test2025_10_17() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UUPUP");
        println(slate.hottestCandidates());
        var amber = slate.tried("AMBER", "PUUFU");
        println(amber.hottestCandidates());
        var haven = slate.tried("HAVEN", "UUUUU");
//        Game mushy = slate.tried("HEARD", "UFFFF");
//        IO.println(mushy.hotCandidates());
//        Game crisp = mushy.tried("CRISP", "UFUFU");
//        IO.println(crisp.hotCandidates());
//        Game done = crisp.tried("GROSS", "PPPPP");
//        assertThat(done.done()).isTrue();
    }

    @Test
    void test2025_10_20() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UPUUU");
        println(slate.hottestCandidates());
        var lurid = slate.tried("LURID", "FUUPU");
        println(lurid.hottestCandidates());
        var lingo = lurid.tried("LINGO", "FFUUF");
        println(lingo.hottestCandidates());

//        Game mushy = slate.tried("HEARD", "UFFFF");
//        IO.println(mushy.hotCandidates());
//        Game crisp = mushy.tried("CRISP", "UFUFU");
//        IO.println(crisp.hotCandidates());
//        Game done = crisp.tried("GROSS", "PPPPP");
//        assertThat(done.done()).isTrue();
    }

    @Test
    void test2025_10_21() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UUUPP");
        println(slate.hottestCandidates());
        var tenor = game.tried("TENOR", "PFUFU");
        println(tenor.hottestCandidates());
        var depot = game.tried("DEPOT", "FFUFP");
        println(depot.hottestCandidates());
    }

    @Test
    void test2025_10_22() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "FUUPU");
        println(slate.hottestCandidates());
        var snout = game.tried("SNOUT", "FPUPF");
        println(snout.hottestCandidates());
//        var depot = game.tried("DEPOT", "FFUFP");
//        IO.println(depot.hottestCandidates());
    }

    @Test
    void test2025_10_24() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UUUPP");
        println(slate.hotCandidatesDescending());
        var bunch = slate.tried("BUNCH", "PFUUU");
        println(bunch.hotCandidatesDescending());
//        var depot = game.tried("DEPOT", "FFUFP");
//        IO.println(depot.hottestCandidates());
    }

    @Test
    void testLobby() {
        var game = new Game(
            Stream.of(
                    "SLATE",
                    "LOLLY",
                    "LORDY",
                    "LOOPY",
                    "LOWLY",
                    "LOONY",
                    "LOBBY"
                )
                .map(Word::new)
                .toList()
        );
        var slate = game.tried("SLATE", "UPUUU");
        var lolly = slate.tried("LOLLY", "FFUUF");
        assertNotCandidate(lolly, "LOWLY");
        var lordy = lolly.tried("LORDY", "FFUUF");
        var loopy = lordy.tried("LOOPY", "FFUUF");
        assertNotCandidate(loopy, "LOWLY");
        assertNotCandidate(loopy, "LOONY");
    }

    //
    @Test
    void testNoTheme() {
        var game = new Game(
            Stream.of(
                    "CLASP",
                    "BORED",
                    "THINE",
                    "THEME",
                    "THEFT",
                    "TONER",
                    "THYNE"
                )
                .map(Word::new)
                .toList()
        );

        var clasp = game.tried("CLASP", "UUUUU");
        var bored = clasp.tried("BORED", "UUUPU");
        assertCandidate(bored, "THEME");
        assertCandidate(bored, "THINE");
        assertCandidate(bored, "THEFT");
        var thine = bored.tried("THINE", "FFUUP");
        assertNotCandidate(thine, "THEME");
        assertThat(thine.wordScores().ratings())
            .singleElement().matches(rating ->
                rating.getValue().word().equals(new Word("THEFT")));
    }

    @Test
    void test2025_10_25() {
        var game = new Game(Word.fromFile(Path.of("words.txt")));
        var slate = game.tried("SLATE", "UUPUF");
        var barge = slate.tried("BARGE", "UFUFF");
        var distribution = barge.distribution();
        var hotCandidatesDescending = barge.hotCandidatesDescending();

        println(distribution);

        hotCandidatesDescending.stream()
            .map(elim ->
                Map.entry(elim, distribution.scoreStr(elim.word())))
            .forEach(System.out::println);
//        var bunch = slate.tried("BUNCH", "");
//        IO.println(bunch.hotCandidatesDescending());
//        var depot = game.tried("DEPOT", "FFUFP");
//        IO.println(depot.hottestCandidates());
    }

    @Test
    void test2025_10_27() {
        var past = Word.fromFile("past.txt");
        var game = new Game(Word.fromFile("words.txt"))
//            .past(past)
            ;

        game = game.tried("CLASP", "UPPUU");
        game = game.tried("FATAL", "UPUUP");
//        game = game.tried("RELAY", "UPPPU");
//        game = game.tried("ANGLE", "FFUFF");
        println(game);

        var distribution = game.distribution();
        println(distribution.distributions().size() + " distributions:");
        distribution.distributions()
            .forEach(System.out::println);
        println();
        var hotCandidatesDescending = game.hotCandidatesDescending();
        println(hotCandidatesDescending.size() + " hot candidates:");
        println(hotCandidatesDescending);
        println();

        var scores = game.wordScores();
        println(scores.scores().size() + " scores:");
        scores.ratings()
            .forEach(System.out::println);
        println();

//        hotCandidatesDescending.stream()
//            .map(elim ->
//                Map.entry(elim, distribution.scoreStr(elim.word())))
//            .forEach(System.out::println);
//        var bunch = slate.tried("BUNCH", "");
//        IO.println(bunch.hotCandidatesDescending());
//        var depot = game.tried("DEPOT", "FFUFP");
//        IO.println(depot.hottestCandidates());
    }

    @Test
    void testPerf() {
        var game = new Game(Word.fromFile("words.txt"));

        game = game.tried("CLASP", "UUUUU");
        println(game);

        var distribution = game.distribution();
        println(distribution.distributions().size() + " distributions:");
        distribution.distributions()
            .forEach(System.out::println);
        println();
        var hotCandidatesDescending = game.hotCandidatesDescending();
        println(hotCandidatesDescending.size() + " hot candidates:");
        println(hotCandidatesDescending);
        println();

        var scores = game.wordScores();
        println(scores.scores().size() + " scores:");
        scores.ratings()
            .forEach(System.out::println);
        println();

//        hotCandidatesDescending.stream()
//            .map(elim ->
//                Map.entry(elim, distribution.scoreStr(elim.word())))
//            .forEach(System.out::println);
//        var bunch = slate.tried("BUNCH", "");
//        IO.println(bunch.hotCandidatesDescending());
//        var depot = game.tried("DEPOT", "FFUFP");
//        IO.println(depot.hottestCandidates());
    }

    private static ListAssert<Map.Entry<Double, WordScore>> assertNotCandidate(Game game, String word) {
        return assertThat(game.wordScores().ratings())
            .noneMatch(rating ->
                rating.getValue().word().equals(new Word(word)));
    }

    private static ListAssert<Map.Entry<Double, WordScore>> assertCandidate(Game game, String word) {
        return assertThat(game.wordScores().ratings())
            .anyMatch(rating ->
                rating.getValue().word().equals(new Word(word)));
    }
}
