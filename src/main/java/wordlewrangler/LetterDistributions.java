package wordlewrangler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("NullableProblems")
public record LetterDistributions(List<LetterDistribution> distributions) {

    public String scoreStr(Word word) {
        return NUMBER_INSTANCE.format(score(word));
    }

    public double score(Word word) {
        return IntStream.range(0, distributions.size())
            .mapToDouble(position ->
                score(position, word.charAt(position)))
            .sum();
    }

    public double score(int position, char c) {
        return distributions.get(position).score(c);
    }

    private static final NumberFormat NUMBER_INSTANCE = DecimalFormat.getNumberInstance(Locale.ROOT);

    @Override
    public String toString() {
        return "{" +
               distributions.stream()
                   .map(Object::toString)
                   .collect(Collectors.joining("/")) +
               "}";
    }
}
