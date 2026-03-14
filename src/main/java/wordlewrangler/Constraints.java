package wordlewrangler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static wordlewrangler.Constraint.*;

final class Constraints {

    public static Set<Constraint> parse(Word guess, String spec) {
        Map<Character, Constraint> yellows = new HashMap<>();
        Map<Character, Constraint> greys = new HashMap<>();
        var letters = guess.letters();
        var specs = spec.toCharArray();
        Set<Constraint> constraints = new HashSet<>();

        // Quick scan for each letter
        for (int index = 0; index < specs.length; index++) {
            var letter = letters[index];
            switch (specs[index]) {
                case 'F' -> constraints.add(new Green(letter, index));
                case 'P' -> yellows.put(letter, new Yellow(letter, index));
                case 'U' -> greys.put(letter, new Grey(letter, index));
                default -> throw new IllegalArgumentException("Invalid constraint spec: " + spec);
            }
        }

        // Let's a have a closer look at each grey
        Map.copyOf(greys).forEach((c, grey) -> {
            // Is there a yellow for it as well
            var yellow = yellows.get(c);
            if (yellow == null) {
                // If not, they grey applies to all slots: The letter is not in the word
                greys.put(c, new Grey(c));
            } else {
                // If so, the yellow constraint should apply to both these slots
                yellows.put(c, yellow.merge(grey));
                // The grey is removed
                greys.remove(c);
            }
        });

        constraints.addAll(yellows.values());
        constraints.addAll(greys.values());
        return constraints;
    }

    private Constraints() {
    }

    static final int[] ALL_POSITIONS = {0, 1, 2, 3, 4};

    static final int[] NOWHERE = new int[0];
}
