package wordlewrangler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

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

    privateµ static final NumberFormat NUMBER_INSTANCE = DecimalFormat.getNumberInstance(Locale.ROOT);
}
