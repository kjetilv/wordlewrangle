package wordlewrangler;

@SuppressWarnings("NullableProblems")
public record WordScore(
    Word word,
    double distribution,
    int eliminated
) {

    @Override
    public String toString() {
        return word + ":" + eliminated + "/" + distribution;
    }
}
