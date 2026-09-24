package match3game;

import static match3game.BoardConstants.BOARD_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public final class Game {

    private Game() {
    }

    static final Random RANDOM = new Random();
    static final char[] SYMBOLS = { 'A', 'B', 'C', 'D', 'E', 'F' };
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

    public static Board cloneBoard(Board board) {
        Element[][] cells = new Element[board.size()][board.size()];
        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.size(); col++) {
                cells[row][col] = board.cells()[row][col];
            }
        }
        return new Board(board.size(), cells);
    }


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
        BoardStatePipe filled = BoardStatePipe.empty()
                                            .fillEmptySpaces()
                                            .processCascade();
        return filled.boardState();
    }

}
