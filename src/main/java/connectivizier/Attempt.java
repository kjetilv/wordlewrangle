package connectivizier;

import java.util.Collection;
import java.util.Collections;

public sealed interface Attempt {

    default boolean blocks(Guess guess) {
        return false;
    }

    default boolean isCorrect() {
        return false;
    }

    default Collection<Word> found() {
        return Collections.emptyList();
    }

    record OneAway(Guess guess) implements Attempt {
    }

    record Exclusives(Guess guess) implements Attempt {

        @Override
        public boolean blocks(Guess guess) {
            return this.guess.overlap(guess).size() >= 2;
        }
    }

    record Wrong(Guess guess) implements Attempt {

        @Override
        public boolean blocks(Guess guess) {
            return this.guess.overlap(guess).size() == 3;
        }
    }

    record Correct(Guess guess) implements Attempt {
        @Override
        public Collection<Word> found() {
            return guess.words();
        }

        @Override
        public boolean isCorrect() {
            return true;
        }
    }
}
