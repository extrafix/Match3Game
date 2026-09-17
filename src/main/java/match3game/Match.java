package match3game;

/**
 * Match — описание одной комбинации на доске.
 *
 * length — длина непрерывной последовательности одинаковых символов.
 */
public record Match(
    MatchDirection direction, 
    int row,
    int col, 
    int length) {
}
