import java.util.*;

//todo enum position with all possible moves so that dont have to calculate every time
//todo mr paige has class position
//todo give index or row&col to position to look up in array to find what moves to look up
//todo a bunch of predicates i=that check if valid i.e isValidRow
//todo state claps that wraps around it it has the heuristics in enum w/ lambda overide
//todo cached heuristic 

public class Board {
    private enum Direction {
        UP(-1, 0),
        DOWN(1, 0),
        LEFT(0, -1),
        RIGHT(0, 1);

        final int deltaRow;
        final int deltaCol;

        Direction(int deltaRow, int deltaCol) {
            this.deltaRow = deltaRow;
            this.deltaCol = deltaCol;
        }

        static Direction fromString(String s) {
            return Direction.valueOf(s.toUpperCase());
        }

        int computeNewIndex(int currentIndex, int cols) {
            return currentIndex + deltaRow * cols + deltaCol;
        }

        boolean isValid(int position, int rows, int cols) {
            int r = position / cols;
            int c = position % cols;
            int newR = r + deltaRow;
            int newC = c + deltaCol;
            return newR >= 0 && newR < rows && newC >= 0 && newC < cols;
        }
    }

    private final byte rows;
    private final byte cols;
    private final byte[] tiles;
    private final byte emptyTileIndex;
    private final boolean goalAtTop; // true = empty top left, false = empty bottom right

    private final String[][] movesCache;
    private int cachedHashCode = 0;
    private boolean hashComputed = false;

    public Board(int rows, int cols, int[] tiles, boolean goalAtTop) {
        if (rows > 127 || cols > 127 || tiles.length > 127) {
            throw new IllegalArgumentException("Board dimensions too large for byte storage");
        }

        this.rows = (byte) rows;
        this.cols = (byte) cols;
        this.tiles = new byte[tiles.length];
        this.goalAtTop = goalAtTop;

        byte empty = -1;
        for (int i = 0; i < tiles.length; i++) {
            this.tiles[i] = (byte) tiles[i];
            if (tiles[i] == 0) {
                empty = (byte) i;
            }
        }
        this.emptyTileIndex = empty;

        // Precompute valid moves for each position on THIS board
        this.movesCache = precomputeAllMoves();
    }

    private Board(byte rows, byte cols, byte[] tiles, byte emptyTileIndex, boolean goalAtTop, String[][] movesCache) {
        this.rows = rows;
        this.cols = cols;
        this.tiles = tiles;
        this.emptyTileIndex = emptyTileIndex;
        this.goalAtTop = goalAtTop;
        this.movesCache = movesCache; // Reuse the same cache
    }

    private String[][] precomputeAllMoves() {
        int n = rows * cols;
        String[][] cache = new String[n][];

        for (int pos = 0; pos < n; pos++) {
            List<String> validMoves = new ArrayList<>(4);
            for (Direction dir : Direction.values()) {
                if (dir.isValid(pos, rows, cols)) {
                    validMoves.add(dir.name());
                }
            }
            cache[pos] = validMoves.toArray(new String[0]);
        }

        return cache;
    }
    public boolean isGoal() {
        int n = rows * cols;

        if (goalAtTop) {
            // Empty tile should be at position 0
            if (tiles[0] != 0) return false;
            for (int i = 1; i < n; i++) {
                if (tiles[i] != i) return false;
            }
            return true;
        } else {
            // Empty tile should be at position n-1
            if (tiles[n - 1] != 0) return false;
            for (int i = 0; i < n - 1; i++) {
                if (tiles[i] != i + 1) return false;
            }
            return true;
        }
    }

    public String[] actions() {
        return movesCache[emptyTileIndex];
    }

    public Board next(String action) {
        Direction dir;
        try {
            dir = Direction.fromString(action);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid move: " + action);
        }

        // Validate that this move is actually valid for current position
        if (!dir.isValid(emptyTileIndex, rows, cols)) {
            throw new IllegalArgumentException("Move is out of bounds: " + action);
        }

        int newIndex = dir.computeNewIndex(emptyTileIndex, cols);

        byte[] copy = Arrays.copyOf(tiles, tiles.length);
        copy[emptyTileIndex] = copy[newIndex];
        copy[newIndex] = 0;

        return new Board(rows, cols, copy, (byte) newIndex, goalAtTop, movesCache);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        Board other = (Board) obj;
        return Arrays.equals(this.tiles, other.tiles);
    }

    @Override
    public int hashCode() {
        if (!hashComputed) {
            cachedHashCode = Arrays.hashCode(tiles);
            hashComputed = true;
        }
        return cachedHashCode;
    }

    @Override
    public String toString() {
        String result = "";

        for (int r = 0; r < rows; r++) {
            // Row values
            for (int c = 0; c < cols; c++) {
                int val = tiles[r * cols + c];
                if (val == 0) {
                    result += "   ";
                } else {
                    result += String.format("%3d", val);
                }
                if (c < cols - 1) result += " |";
            }
            result += "\n";

            // Separator line
            if (r < rows - 1) {
                for (int c = 0; c < cols; c++) {
                    result += "----";
                    if (c < cols - 1) result += "+";
                }
                result += "\n";
            }
        }
        return result;
    }

    public void display() {
        System.out.println(this.toString());
    }

    public int[] getTiles() {
        int[] intTiles = new int[tiles.length];
        for (int i = 0; i < tiles.length; i++) {
            intTiles[i] = tiles[i];
        }
        return intTiles;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("not all parameters entered");
            return;
        }

        int rows = 0, cols = 0;
        boolean goalAtTop = false;
        boolean goalAtBottom = false;

        List<Integer> tileList = new ArrayList<>();
        List<String> moves = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String a = args[i].toLowerCase();

            switch (a) {
                case "-rows":
                    rows = Integer.parseInt(args[++i]);
                    break;
                case "-columns":
                    cols = Integer.parseInt(args[++i]);
                    break;
                case "-size":
                    rows = cols = Integer.parseInt(args[++i]);
                    break;
                case "-top":
                    goalAtTop = true;
                    break;
                case "-bottom":
                    goalAtBottom = true;
                    break;
                default:
                    if (isMove(a)) moves.add(a.toUpperCase());
                    else tileList.add(turnToTile(a));
            }
        }

        if (rows <= 0 || cols <= 0) {
            System.err.println("Error: didn't specify board size");
            return;
        }

        int expectedTiles = rows * cols;
        if (tileList.size() != expectedTiles) {
            System.err.println("Error: not enough tiles provided (" + tileList.size() + " provided, need " + expectedTiles + ")");
            return;
        }
        //check for duplicates and tile value range
        Set<Integer> seen = new HashSet<>();
        for (int t : tileList) {
            if (t < 0 || t >= expectedTiles) {
                System.err.println("Error: Tile " + t + " out of range (0-" + (expectedTiles - 1) + ").");
                return;
            }
            if (!seen.add(t)) {
                System.err.println("Error: Duplicate tile (" + t + ").");
                return;
            }
        }

        //verify all required tiles exist
        for (int v = 0; v < expectedTiles; v++) {
            if (!seen.contains(v)) {
                System.err.println("Error: Missing tile " + v + ".");
                return;
            }
        }

        if (goalAtTop && goalAtBottom) {
            System.err.println("Error: Cannot have goal be at both top and bottom.");
            return;
        }

        // default to bottom if neither specified
        if (!goalAtTop && !goalAtBottom) {
            goalAtBottom = true;
        }

        int[] tiles = new int[expectedTiles];
        for (int i = 0; i < expectedTiles; i++) tiles[i] = tileList.get(i);

        Board board = new Board(rows, cols, tiles, goalAtTop);

        board.display();

        for (String move : moves) {
            System.out.println(move + "\n");
            try {
                board = board.next(move);
                board.display();
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                break;
            }
        }

        if (board.isGoal()) System.out.println("Solved");
        else System.out.println("Not Solved");
    }


    private static boolean isMove(String s) {
        String up = s.toUpperCase();
        return up.equals("LEFT") || up.equals("RIGHT") || up.equals("UP") || up.equals("DOWN");
    }

    private static int turnToTile(String s) {
        if (s.equals(".") || s.equals("0")) return 0;
        try {
            int num = Integer.parseInt(s);
            if (num < 0 || num > 15) {
                throw new IllegalArgumentException("Tile out of range");
            }
            return num;
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid tile value: " + s);
            System.exit(1);
            return -1;
        }
    }
}