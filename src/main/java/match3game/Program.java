package match3game;


public final class Program {

    private Program() {
    }

    public static void main(String[] args) {
        BoardState bs = Game.initializeGame();

        while (true) {
            Game.draw(bs.board());
            bs = Game.readMove(bs);
            bs = Game.processCascade(bs);
        }
    }
}
