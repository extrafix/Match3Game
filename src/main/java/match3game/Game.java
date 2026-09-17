package match3game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Game — единое «пространство имён» для всех функций игры.
 *
 * Это единственный допустимый в курсе глобальный синглтон: класс выступает
 * хранилищем чистых функций, а не точкой связи между компонентами.
 * Предметные типы (Element, Board, BoardState, Match) остаются данными
 * без методов: вызываем {@code foo(obj)}, а не {@code obj.foo()}.
 *
 * В C# это {@code static partial class Game}. В Java нет partial-классов,
 * поэтому все функции собраны здесь, а конструктор закрыт.
 */
public final class Game {

    private Game() {
    }

    private static final Random RANDOM = new Random();
    private static final char[] SYMBOLS = { 'A', 'B', 'C', 'D', 'E', 'F' };
    private static final int BOARD_SIZE = 8;
    private static final Scanner SCANNER = new Scanner(System.in);


    public static void draw(Board board) {
        System.out.println("  0 1 2 3 4 5 6 7");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board.cells()[i][j].symbol() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // --- cloneBoard: глубокое копирование доски ---
    // cells — ссылочный массив, поэтому поверхностной копии record недостаточно.
    // Element иммутабелен, достаточно скопировать ссылки на фишки в новый массив.

    public static Board cloneBoard(Board board) {
        Element[][] cells = new Element[board.size()][board.size()];
        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.size(); col++) {
                cells[row][col] = board.cells()[row][col];
            }
        }
        return new Board(board.size(), cells);
    }

    // --- readMove: ввод хода и новое состояние ---
    // Формат: "y x y1 x1" (как в курсе). "q" — выход.
    // Исходная доска не меняется: работаем с клоном.

    public static BoardState readMove(BoardState bs) {
        System.out.println(">");
        String input = SCANNER.nextLine();
        if (input == null) {
            System.exit(0);
        }
        input = input.trim();
        if ("q".equals(input)) {
            System.exit(0);
        }

        String[] coords = input.split(" ");
        if (coords.length < 4) {
            System.out.println("Нужны четыре координаты: y x y1 x1 (или q для выхода)");
            return bs;
        }

        int x;
        int y;
        int x1;
        int y1;
        try {
            x = Integer.parseInt(coords[1]);
            y = Integer.parseInt(coords[0]);
            x1 = Integer.parseInt(coords[3]);
            y1 = Integer.parseInt(coords[2]);
        } catch (NumberFormatException ex) {
            System.out.println("Координаты должны быть целыми числами");
            return bs;
        }

        if (!inBounds(bs.board(), x, y) || !inBounds(bs.board(), x1, y1)) {
            System.out.println("Координаты вне доски");
            return bs;
        }

        Board board = cloneBoard(bs.board());
        Element e = board.cells()[x][y];
        board.cells()[x][y] = board.cells()[x1][y1];
        board.cells()[x1][y1] = e;
        return new BoardState(board, bs.score());
    }

    private static boolean inBounds(Board board, int row, int col) {
        return row >= 0 && row < board.size() && col >= 0 && col < board.size();
    }

    public static BoardState initializeGame() {
        BoardState empty = new BoardState(new Board(BOARD_SIZE), 0);
        BoardState filled = fillEmptySpaces(empty);
        return processCascade(filled);
    }


    public static List<Match> findMatches(Board board) {
        List<Match> matches = new ArrayList<>();

        for (int row = 0; row < board.size(); row++) {
            int startCol = 0;
            for (int col = 1; col < board.size(); col++) {
                if (board.cells()[row][startCol].symbol() == Element.EMPTY) {
                    startCol = col;
                    continue;
                }

                if (board.cells()[row][col].symbol() == Element.EMPTY) {
                    addMatchIfValid(matches, row, startCol, col - startCol, MatchDirection.HORIZONTAL);
                    startCol = col + 1;
                    continue;
                }

                if (board.cells()[row][col].symbol() != board.cells()[row][startCol].symbol()) {
                    addMatchIfValid(matches, row, startCol, col - startCol, MatchDirection.HORIZONTAL);
                    startCol = col;
                } else if (col == board.size() - 1) {
                    addMatchIfValid(matches, row, startCol, col - startCol + 1, MatchDirection.HORIZONTAL);
                }
            }
        }

        for (int col = 0; col < board.size(); col++) {
            int startRow = 0;
            for (int row = 1; row < board.size(); row++) {
                if (board.cells()[startRow][col].symbol() == Element.EMPTY) {
                    startRow = row;
                    continue;
                }

                if (board.cells()[row][col].symbol() == Element.EMPTY) {
                    addMatchIfValid(matches, startRow, col, row - startRow, MatchDirection.VERTICAL);
                    startRow = row + 1;
                    continue;
                }

                if (board.cells()[row][col].symbol() != board.cells()[startRow][col].symbol()) {
                    addMatchIfValid(matches, startRow, col, row - startRow, MatchDirection.VERTICAL);
                    startRow = row;
                } else if (row == board.size() - 1) {
                    addMatchIfValid(matches, startRow, col, row - startRow + 1, MatchDirection.VERTICAL);
                }
            }
        }

        return matches;
    }

    private static void addMatchIfValid(List<Match> matches, 
                                        int row, 
                                        int col,
                                        int length, 
                                        MatchDirection direction) {
        if (length >= 3) {
            matches.add(new Match(direction, row, col, length));
        }
    }


    public static BoardState removeMatches(BoardState currentState, List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            return currentState;
        }

        Element[][] markedCells = markCellsForRemoval(currentState.board(), matches);
        Element[][] gravityAppliedCells = applyGravity(markedCells, currentState.board().size());

        int removedCount = matches.stream().mapToInt(match -> match.length()).sum();

        int newScore = currentState.score() + calculateScore(removedCount);

        return new BoardState(
                new Board(currentState.board().size(), gravityAppliedCells),
                newScore
        );
    }

    private static Element[][] markCellsForRemoval(Board board, List<Match> matches) {
        Element[][] newCells = cloneCells(board.cells(), board.size());
        for (Match match : matches) {
            for (int i = 0; i < match.length(); i++) {
                int row = match.direction() == MatchDirection.HORIZONTAL ? 
                    match.row() : match.row() + i;
                int col = match.direction() == MatchDirection.HORIZONTAL ? 
                    match.col() + i : match.col();
                newCells[row][col] = new Element(Element.EMPTY);
            }
        }
        return newCells;
    }

    private static Element[][] applyGravity(Element[][] cells, int size) {
        Element[][] newCells = new Element[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                newCells[row][col] = new Element(Element.EMPTY);
            }
        }

        for (int col = 0; col < size; col++) {
            int newRow = size - 1;
            for (int row = size - 1; row >= 0; row--) {
                if (cells[row][col].symbol() != Element.EMPTY) {
                    newCells[newRow][col] = cells[row][col];
                    newRow--;
                }
            }
        }
        return newCells;
    }

    private static int calculateScore(int removedCount) {
        return removedCount * 10;
    }

    public static BoardState fillEmptySpaces(BoardState currentState) {
        if (currentState.board().cells() == null) {
            return currentState;
        }

        int size = currentState.board().size();
        Element[][] newCells = cloneCells(currentState.board().cells(), size);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (newCells[row][col].symbol() == Element.EMPTY) {
                    newCells[row][col] = new Element(SYMBOLS[RANDOM.nextInt(SYMBOLS.length)]);
                }
            }
        }
        return new BoardState(new Board(size, newCells), currentState.score());
    }

    public static BoardState processCascade(BoardState currentState) {
        List<Match> matches = findMatches(currentState.board());
        if (matches.isEmpty()) {
            return currentState;
        }
        BoardState afterRemove = removeMatches(currentState, matches);
        BoardState afterFill = fillEmptySpaces(afterRemove);
        return processCascade(afterFill);
    }

    private static Element[][] cloneCells(Element[][] cells, int size) {
        Element[][] copy = new Element[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                copy[row][col] = new Element(cells[row][col].symbol());
            }
        }
        return copy;
    }
}
