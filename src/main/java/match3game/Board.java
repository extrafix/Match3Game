package match3game;

public record Board(
    int size, 
    Element[][] cells) {

    public Board(int size) {
        this(size, emptyCells(size));
    }

    private static Element[][] emptyCells(int size) {
        Element[][] cells = new Element[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                cells[x][y] = new Element(Element.EMPTY);
            }
        }
        return cells;
    }
}
