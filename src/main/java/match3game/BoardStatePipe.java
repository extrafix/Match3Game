package match3game;

import static match3game.BoardConstants.BOARD_SIZE;

public record BoardStatePipe(
    BoardState boardState) {

    public static BoardStatePipe empty(){
        BoardState emptyBoardState = new BoardState(
                                            new Board(BOARD_SIZE), 0
                                    );
        return new BoardStatePipe(emptyBoardState);
    }

    public BoardStatePipe fillEmptySpaces(){

        BoardState nextBoardState = BoardUtils.fillEmptySpaces(boardState);
        return new BoardStatePipe(nextBoardState);
    }

    public BoardStatePipe processCascade() {
        
        BoardState nextBoardState = BoardUtils.processCascade(boardState);
        return new BoardStatePipe(nextBoardState);
    }
}