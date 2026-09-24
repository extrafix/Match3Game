package match3game;

import java.util.List;

/**
 * Неинтерактивная проверка ядра: комбинации, гравитация, счёт, каскад.
 */
public final class GameLogicCheck {

    private GameLogicCheck() {
    }

    public static void main(String[] args) {
        checkHorizontalMatch();
        checkVerticalMatch();
        checkNoMatch();
        checkGravityAndScore();
        checkInitializeHasNoMatches();
        System.out.println("Все проверки пройдены.");
    }

    private static void checkHorizontalMatch() {
        Board board = checkerboard();
        board.cells()[0][0] = new Element('E');
        board.cells()[0][1] = new Element('E');
        board.cells()[0][2] = new Element('E');
        List<Match> matches = BoardUtils.findMatches(board);
        assertEq(1, matches.size(), "горизонтальная комбинация");
        Match m = matches.get(0);
        assertEq(MatchDirection.HORIZONTAL, m.direction(), "направление");
        assertEq(0, m.row(), "row");
        assertEq(0, m.col(), "col");
        assertEq(3, m.length(), "length");
    }

    private static void checkVerticalMatch() {
        Board board = checkerboard();
        board.cells()[1][4] = new Element('F');
        board.cells()[2][4] = new Element('F');
        board.cells()[3][4] = new Element('F');
        board.cells()[4][4] = new Element('F');
        List<Match> matches = BoardUtils.findMatches(board);
        assertEq(1, matches.size(), "вертикальная комбинация");
        Match m = matches.get(0);
        assertEq(MatchDirection.VERTICAL, m.direction(), "направление");
        assertEq(1, m.row(), "row");
        assertEq(4, m.col(), "col");
        assertEq(4, m.length(), "length");
    }

    private static void checkNoMatch() {
        Board board = checkerboard();
        board.cells()[0][0] = new Element('E');
        board.cells()[0][1] = new Element('E');
        List<Match> matches = BoardUtils.findMatches(board);
        assertEq(0, matches.size(), "нет комбинации из двух");
    }

    private static void checkGravityAndScore() {
        Board board = checkerboard();
        board.cells()[7][0] = new Element('E');
        board.cells()[7][1] = new Element('E');
        board.cells()[7][2] = new Element('E');
        board.cells()[6][0] = new Element('F');
        BoardState before = new BoardState(board, 0);
        BoardState after = BoardUtils.removeMatches(before, BoardUtils.findMatches(board));
        assertEq('F', after.board().cells()[7][0].symbol(), "F упала вниз на место удалённой E");
        assertEq(30, after.score(), "10 очков за фишку");
        assertEq(Element.EMPTY, after.board().cells()[0][0].symbol(), "сверху колонки появилась пустота");
    }

    private static void checkInitializeHasNoMatches() {
        for (int i = 0; i < 20; i++) {
            BoardState bs = Game.initializeGame();
            List<Match> matches = BoardUtils.findMatches(bs.board());
            assertEq(0, matches.size(), "после инициализации нет комбинаций");
            assertEq(8, bs.board().size(), "размер доски");
        }
    }

    private static Board checkerboard() {
        Board board = new Board(8);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char symbol = ((r + c) % 2 == 0) ? 'C' : 'D';
                board.cells()[r][c] = new Element(symbol);
            }
        }
        return board;
    }

    private static void assertEq(Object expected, Object actual, String title) {
        if (!expected.equals(actual)) {
            throw new AssertionError(title + ": ожидалось " + expected + ", получено " + actual);
        }
    }
}
