import connectivizier.Connections;
import connectivizier.Guess;
import connectivizier.Word;

void main() {
    var connections = Connections.create(
//        """
//            UBER	SWAN	LOVE	VIAL
//            GOOSE	GOOGLE	FOWL	ZOOM
//            YAHOO	SIREN	MIEN	DODO
//            PHOTOSHOP	FOLK	GOOF	OFFAL
//            """
        """
            DIRECT DRIVE GUIDE PILOT
            DOC MID TIFF ZIP
            COAT FILM LEAF SHEET
            AIR ELECTRIC RHYTHM SLIDE
            """
    );

    System.out.println(connections.string());

//    connections.guesses().map(Guess::words)
//        .map(Word::printRow)
//        .forEach(System.out::println);

//    connections.guesses()
//        .map(Guess::toShortString)
//        .gather(Gatherers.windowFixed(32))
//        .forEach(guesses -> System.out.println(String.join(" ", guesses)));

    System.out.println("Total guesses: " + connections.guesses().count());
    System.out.println("Total distinct guesses: " + connections.guesses().distinct().count());

    var ab9F = connections.add("9ABF");
    System.out.println(ab9F);
}