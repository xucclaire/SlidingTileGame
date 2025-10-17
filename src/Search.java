import java.util.*;

public class Search {
    public enum Kind { TREE, GRAPH }

    public interface Action {
        double cost();
    }

    public interface State {
        boolean isGoal();
        State next(Action action);
        ArrayList<? extends Action> actions();
        default double heuristic() { return 0.0; }
    }

    private static int statesCreated = 0;
    private static int queries = 0;
    private static int expanded = 0;
    private static int improved = 0;
    private static int maxFrontier = 0;

    public static void resetStats() {
        statesCreated = 0;
        queries = 0;
        expanded = 0;
        improved = 0;
        maxFrontier = 0;
        Node.clear();
    }

    public static void recordStatesCreated() { statesCreated++; }
    public static void recordQuery() { queries++; }
    public static void recordExpanded() { expanded++; }
    public static void recordImproved() { improved++; }

    public static void updateMaxFrontier(int size) {
        if (size > maxFrontier) maxFrontier = size;
    }

    public static int createdStates() { return statesCreated; }
    public static int queries() { return queries; }
    public static int expanded() { return expanded; }
    public static int improved() { return improved; }
    public static int maxFrontier() { return maxFrontier; }

    public abstract static class SearchAlgorithm {
        protected final boolean unique; //if unique = true -> graph search, false -> tree search
        protected final boolean trace;

        public SearchAlgorithm(Kind kind, boolean trace) {
            this.unique = (kind == Kind.GRAPH);
            this.trace = trace;
        }

        protected Node find(State state) {
            recordQuery();
            return Node.find(state, this.unique);
        }

        public abstract Node search(State start);

        public Iterable<Action> solve(State start) {
            resetStats(); // clear previous run
            Node goal = search(start);
            if (goal == null) return null;
            return goal.solution();
        }
    }

    public static class Node {
        private static HashMap<State, Node> nodes = new HashMap<>();

        public final State state;
        public Node parent;
        public Action action;
        public int depth = 0;
        public double cost = 0;

        public Node(State s) {
            this.state = s;
            recordStatesCreated();
        }

        public static void clear() {
            nodes.clear();
        }

        public static Node find(State s, boolean unique) {
            if (!unique) {
                return new Node(s);
            }
            if (!nodes.containsKey(s)) {
                nodes.put(s, new Node(s));
            }
            return nodes.get(s);
        }

        public void update(Node parent, Action action) {
            this.parent = parent;
            this.action = action;
            if (parent != null && action != null) {
                this.depth = parent.depth + 1;
                this.cost = parent.cost + action.cost();
            }
        }

        public Iterable<Action> solution() {
            ArrayList<Action> path = new ArrayList<>();
            for (Node n = this; n.parent != null; n = n.parent)
                path.add(0, n.action);
            return path;
        }
    }

    //
    //DFS
    //

    public static class DepthFirstSearch extends SearchAlgorithm {
        public DepthFirstSearch(Kind kind, boolean trace) {
            super(kind, trace);
        }

        @Override
        public Node search(State start) {
            Stack<Node> stack = new Stack<>();
            Set<State> visited;
            if (unique) visited = new HashSet<>();
            else visited = null;

            Node root = find(start);
            stack.push(root);
            if (unique) visited.add(start);

            while (!stack.isEmpty()) {
                updateMaxFrontier(stack.size());
                Node node = stack.pop();
                recordExpanded();
                if (trace) System.out.println("DFS pop: " + node.state);

                if (node.state.isGoal()) return node;

                for (Action a : node.state.actions()) {
                    State t = node.state.next(a);
                    Node child = find(t);
                    if (!unique || !visited.contains(t)) {
                        child.update(node, a);
                        stack.push(child);
                        if (unique) visited.add(t);
                        if (trace) System.out.println("  -> " + a + " => " + t);
                    }
                }
            }
            return null;
        }
    }

    //
    //BFS
    //

    public static class BreadthFirstSearch extends SearchAlgorithm {
        public BreadthFirstSearch(Kind kind, boolean trace) {
            super(kind, trace);
        }

        @Override
        public Node search(State start) {
            Queue<Node> queue = new Queue<>();
            Map<State, Boolean> visited = unique ? new HashMap<>() : null;

            Node root = find(start);
            queue.enqueue(root);
            if (unique) visited.put(start, true);

            while (!queue.isEmpty()) {
                updateMaxFrontier(queue.size());
                Node node = queue.dequeue();
                recordExpanded();
                if (trace) System.out.println("BFS pop: " + node.state);

                if (node.state.isGoal()) return node;

                for (Action a : node.state.actions()) {
                    State t = node.state.next(a);
                    Node child = find(t);
                    if (!unique || !visited.containsKey(t)) {
                        child.update(node, a);
                        queue.enqueue(child);
                        if (unique) visited.put(t, true);
                        if (trace) System.out.println("  -> " + a + " => " + t);
                    }
                }
            }
            return null;
        }
    }

    //
    // UCS
    //

    public static class UniformCostSearch extends SearchAlgorithm {
        public UniformCostSearch(Kind kind, boolean trace) {
            super(kind, trace);
        }

        @Override
        public Node search(State start) {
            PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
            Map<State, Double> best = new HashMap<>();

            Node root = find(start);
            root.cost = 0;
            frontier.add(root);
            best.put(root.state, 0.0);

            while (!frontier.isEmpty()) {
                updateMaxFrontier(frontier.size());
                Node node = frontier.poll();
                recordExpanded();
                if (trace) System.out.printf("UCS pop: %s (g=%.2f)%n", node.state, node.cost);

                if (node.state.isGoal()) return node;

                for (Action a : node.state.actions()) {
                    State t = node.state.next(a);
                    Node child = find(t);
                    double g = node.cost + a.cost();

                    if (!best.containsKey(t) || g < best.get(t)) {
                        recordImproved();
                        child.update(node, a);
                        child.cost = g;
                        frontier.add(child);
                        best.put(t, g);
                        if (trace) System.out.printf("  -> %s => %s (g=%.2f)%n", a, t, g);
                    }
                }
            }
            return null;
        }
    }

    //
    // DLS
    //

    public static class DepthLimitedSearch extends SearchAlgorithm {
        private final int limit;

        public DepthLimitedSearch(Kind kind, boolean trace, int limit) {
            super(kind, trace);
            this.limit = limit;
        }

        @Override
        public Node search(State start) {
            return dls(new Node(start), limit, new HashMap<>());
        }

        private Node dls(Node node, int limit, Map<State, Boolean> path) {
            recordExpanded();
            if (trace) System.out.println("DLS visit (limit " + limit + "): " + node.state);
            if (node.state.isGoal()) return node;
            if (limit == 0) return null;

            if (path.containsKey(node.state)) return null;
            path.put(node.state, true);

            for (Action a : node.state.actions()) {
                State t = node.state.next(a);
                Node child = new Node(t);
                child.update(node, a);
                if (trace) System.out.println("  -> " + a + " => " + t);
                Node result = dls(child, limit - 1, path);
                if (result != null) {
                    path.remove(node.state);
                    return result;
                }
            }
            path.remove(node.state);
            return null;
        }
    }

    //
    // IDS
    //

    public static class IterativeDeepeningSearch extends SearchAlgorithm {
        public IterativeDeepeningSearch(Kind kind, boolean trace) {
            super(kind, trace);
        }

        @Override
        public Node search(State start) {
            for (int depth = 0;; depth++) {
                if (trace) System.out.println("ID : depth = " + depth);
                Node result;
                if (unique) result = new DepthLimitedSearch(Kind.GRAPH, trace, depth).search(start);
                else result = new DepthLimitedSearch(Kind.TREE, trace, depth).search(start);
                if (result != null) return result;
            }
        }
    }

    //
    // GBFS
    //

    public static class GreedyBestFirstSearch extends SearchAlgorithm {
        public GreedyBestFirstSearch(Kind kind, boolean trace) {
            super(kind, trace);
        }

        @Override
        public Node search(State start) {
            PriorityQueue<Node> frontier = new PriorityQueue<>(new Comparator<Node>() {
                @Override
                public int compare(Node n1, Node n2) {
                    return Double.compare(n1.state.heuristic(), n2.state.heuristic());
                }
            });
            Set<State> visited;
            if (unique) visited = new HashSet<>();
            else visited = null;

            Node root = find(start);
            frontier.add(root);
            if (unique) visited.add(root.state);

            while (!frontier.isEmpty()) {
                updateMaxFrontier(frontier.size());
                Node node = frontier.poll();
                recordExpanded();
                if (trace) System.out.printf("GBFS pop: %s (h=%.2f)%n", node.state, node.state.heuristic());

                if (node.state.isGoal()) return node;

                for (Action a : node.state.actions()) {
                    State b = node.state.next(a);
                    Node child = find(b);
                    if (!unique || !visited.contains(b)) {
                        child.update(node, a);
                        frontier.add(child);
                        if (unique) visited.add(b);
                        if (trace) System.out.printf("  -> %s => %s (h=%.2f)%n", a, b, b.heuristic());
                    }
                }
            }
            return null;
        }
    }

    //
    // A*
    //

    public static class AStarSearch extends SearchAlgorithm {
        public AStarSearch(Kind kind, boolean trace) {
            super(kind, trace);
        }

        @Override
        public Node search(State start) {
            PriorityQueue<Node> frontier = new PriorityQueue<>(new Comparator<Node>() {
                @Override
                public int compare(Node n1, Node n2) {
                    double f1 = n1.cost + n1.state.heuristic();
                    double f2 = n2.cost + n2.state.heuristic();
                    return Double.compare(f1, f2);
                }
            });
            HashMap<State, Double> best = new HashMap<>();

            Node root = find(start);
            root.cost = 0;
            frontier.add(root);
            best.put(root.state, 0.0);

            while (!frontier.isEmpty()) {
                updateMaxFrontier(frontier.size());
                Node node = frontier.poll();
                recordExpanded();
                if (trace) System.out.printf("A* pop: %s (f=%.2f)%n",
                        node.state, node.cost + node.state.heuristic());

                if (node.state.isGoal()) return node;

                for (Action a : node.state.actions()) {
                    State b = node.state.next(a);
                    Node child = find(b);
                    double c = node.cost + a.cost();

                    if (!best.containsKey(b) || c < best.get(b)) {
                        recordImproved();
                        child.update(node, a);
                        child.cost = c;
                        frontier.add(child);
                        best.put(b, c);
                        if (trace)
                            System.out.printf("  -> %s => %s (c=%.2f, h=%.2f)%n",
                                    a, b, c, b.heuristic());
                    }
                }
            }
            return null;
        }
    }
}