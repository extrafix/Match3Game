package match3game;

import static match3game.Game.RANDOM;
import static match3game.Game.SYMBOLS;

import java.util.ArrayList;
import java.util.List;

public class BoardUtils {

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

    private static Element[][] cloneCells(Element[][] cells, int size) {
        Element[][] copy = new Element[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                copy[row][col] = new Element(cells[row][col].symbol());
            }
        }
        return copy;
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


    public static BoardState processCascade(BoardState currentState) {
        List<Match> matches = findMatches(currentState.board());
        if (matches.isEmpty()) {
            // прерывание
            return currentState;
        }
        BoardState afterRemove = removeMatches(currentState, matches);
        BoardState afterFill = fillEmptySpaces(afterRemove);
        return processCascade(afterFill);
    }
}