import java.util.*;

public class Board {

    private final int rows;
    private final int cols;
    private final int[] tiles;
    private final int emptyTileIndex;
    private final boolean goalAtTop; // true = empty top left, false = empty bottom right

    public Board(int rows, int cols, int[] tiles, boolean goalAtTop) {
        this.rows = rows;
        this.cols = cols;
        this.tiles = Arrays.copyOf(tiles, tiles.length);
        this.goalAtTop = goalAtTop;

        int empty = -1;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] == 0) {
                empty = i;
                break;
            }
        }
        this.emptyTileIndex = empty;
    }

    public boolean isGoal() {
        int n = rows * cols;
        for (int i = 0; i < n - 1; i++) {
            if(goalAtTop){
                if(tiles[i] == i){
                    return true;
                }
            }
            if (tiles[i] != i + 1) return false;
        }
        if (goalAtTop) return tiles[0] == 0;
        else return tiles[n - 1] == 0;
    }

    public ArrayList<String> actions() {
        ArrayList<String> actions = new ArrayList<>();
        int r = emptyTileIndex / cols;
        int c = emptyTileIndex % cols;

        if (r > 0) actions.add("UP");
        if (r < rows - 1) actions.add("DOWN");
        if (c > 0) actions.add("LEFT");
        if (c < cols - 1) actions.add("RIGHT");

        return actions;
    }

    public Board next(String action) {
        int[] copy = Arrays.copyOf(tiles, tiles.length);
        int r = emptyTileIndex / cols;
        int c = emptyTileIndex % cols;
        int newR = r, newC = c;

        if (action.toUpperCase().equals("UP")) {
            newR = r - 1;
        } else if (action.toUpperCase().equals("DOWN")) {
            newR = r + 1;
        } else if (action.toUpperCase().equals("LEFT")) {
            newC = c - 1;
        } else if (action.toUpperCase().equals("RIGHT")) {
            newC = c + 1;
        } else {
            throw new IllegalArgumentException("Invalid move: " + action);
        }

        if (newR < 0 || newR >= rows || newC < 0 || newC >= cols)
            throw new IllegalArgumentException("Move is out of bounds: " + action);

        int newIndex = newR * cols + newC;
        copy[emptyTileIndex] = copy[newIndex];
        copy[newIndex] = 0;

        return new Board(rows, cols, copy, goalAtTop);
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
        return Arrays.hashCode(tiles);
    }

    @Override
    public String toString() {
        String result = "";
        for (int r = 0; r < rows; r++) {
            // Row values
            for (int c = 0; c < cols; c++) {
                int val = tiles[r * cols + c];
                if (val == 0) result += "   ";
                else result += String.format("%3d", val);
                if (c < cols - 1) result += " |";
            }
            result += "\n";
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
        return Arrays.copyOf(tiles, tiles.length);
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

        int[] tiles = new int[expectedTiles];
        for (int i = 0; i < expectedTiles; i++) tiles[i] = tileList.get(i);

        Board board = new Board(rows, cols, tiles, goalAtTop);

        board.display();

        for (String move : moves) {
            System.out.println(move.toUpperCase() + "\n");
            try {
                board = board.next(move);
                board.display();
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid move: " + move);
                break;
            }
            System.out.println();
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
            if (num < 0 || num > 15) throw new IllegalArgumentException();
            return num;
        } catch (Exception e) {
            System.err.println("Invalid tile: " + s);
            System.exit(1);
            return -1;
        }
    }
}