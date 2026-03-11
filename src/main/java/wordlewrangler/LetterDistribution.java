package wordlewrangler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public record LetterDistribution(
    int position,
    List<LetterCount> letterCounts,
    int total
) {

    public LetterDistribution(int position, List<LetterCount> letterCounts) {
        this(
            position,
            letterCounts,
            letterCounts.stream().mapToInt(LetterCount::count).sum()
        );
    }

    public double score(char c) {
        for (var letterCount : letterCounts) {
            if (letterCount.c() == c) {
                return score(letterCount);
            }
        }
        return 0d;
    }

    private double score(LetterCount letterCount) {
        return 1.0d * letterCount.count() / total;
    }

    private static final NumberFormat NUMBER_INSTANCE = DecimalFormat.getNumberInstance(Locale.ROOT);

    @Override
    public String toString() {
        return position + "->" +
               letterCounts.stream()
                   .map(
                       lc ->
                           lc.c() + ":" + NUMBER_INSTANCE.format(score(lc))
                   )
                   .collect(Collectors.joining(" "));
    }
}
