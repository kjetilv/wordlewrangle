package wordlewrangler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public record LetterDistributions(List<LetterDistribution> distributions) {

    public String scoreStr(Word word) {
        return NUMBER_INSTANCE.format(score(word));
    }

    public double score(Word word) {
        double sum = 0d;
        for (int i = 0; i < distributions.size(); i++) {
            sum += score(i, word.letters()[i]);
        }
        return sum;
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
