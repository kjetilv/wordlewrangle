package wordlewrangler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record WordScores(
    List<WordScore> scores,
    int eliminatedMax,
    double distributionMax
) {

    public static final WordScores EMPTY =
        new WordScores(Collections.emptyList(), 0, 0.0);

    public List<Map.Entry<Double, WordScore>> ratings() {
        return scores.stream()
            .map(score ->
                Map.entry(score(score), score))
            .sorted(Map.Entry.<Double, WordScore>comparingByKey().reversed())
            .toList();
    }

    private double score(WordScore score) {
        return (score.distribution() / distributionMax + 4d * score.eliminated() / eliminatedMax) / 5;
    }
}
