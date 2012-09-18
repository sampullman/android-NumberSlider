package mindsnacks.challenges.numberslider;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import java.util.Collection;

import android.util.Log;

public class Game {

    boolean hash, scrambled;
    volatile boolean cancel=false;

    int dim;
    int[][] board;

    Point emptyP;

    Random rand = new Random();

    ArrayList<Move> solution = new ArrayList<Move>();
    ArrayList<Move> userMoves = new ArrayList<Move>();

    public Game(int dim) {
	this.dim = dim;
	board = new int[dim][dim];
	userMoves = new ArrayList<Move>();
	initBoard();
	scrambleBoard();
    }

    /** Initializes the board. */
    public void initBoard() {
	for(int i=0;i<dim;i+=1) {
	    for(int j=0;j<dim;j+=1) {
		board[i][j] = (j * dim) + i + 1;
	    }
	}
	board[dim-1][dim-1] = -1;
	emptyP = new Point(dim-1, dim-1);
    }

    /** Scrambles the board by computing a number of moves.
	For efficiency, a move will not be made if it returns to the
	state it just left. */
    public void scrambleBoard() {
	SearchNode node = new SearchNode(board, emptyP), prev2Node, temp;
	prev2Node = node;
	ArrayList<SearchNode> nodes;
	int n = (dim == 3) ? 50 : 30;
	for(int i=0;i<n;i+=1) {
	    nodes = getNeighbors(node);
	    temp = node;
	    node = nodes.remove(rand.nextInt(nodes.size()));
	    if(node.equals(prev2Node))
		node = nodes.remove(rand.nextInt(nodes.size()));
	    if(i > 0) {
		prev2Node = temp;
	    }
	}
	board = node.board;
	emptyP = node.emptyP;
	userMoves.clear();
	scrambled = true;
    }

    /** Determines whether the user's click is a legal move */
    public boolean isLegalMove(int x, int y) {
	if(x == emptyP.pX && y == emptyP.pY) {
	    return false;
	} else {
	    return (x == emptyP.pX) || (y == emptyP.pY);
	}
    }

    public boolean isSolved() {
	SearchNode n = new SearchNode(board, emptyP);
	return n.isSolved();
    }

    /** Performs a move by updating the board.
	The move must be legal! */
    public void performMove(int x, int y, boolean saveMove) {
	int temp, dir;
	if(emptyP.pX == x) {
	    dir = (y > emptyP.pY) ? 1 : -1;
	    for(int j=emptyP.pY;j!=y;j+=dir) {
		board[x][j] = board[x][j+dir];
	    }
	} else {
	    dir = (x > emptyP.pX) ? 1 : -1;
	    for(int i=emptyP.pX;i!=x;i+=dir) {
		board[i][y] = board[i+dir][y];
	    }
	}
	board[x][y] = -1;
	if(saveMove)
	    userMoves.add(new Move(emptyP, new Point(x, y)));
	emptyP = new Point(x, y);
    }

    /** Cancels the running instance of findSolution. */
    public void cancel(boolean cancel) {
	this.cancel = cancel;
    }

    /** Computes the optimal solution using A-star search.
	The heuristic used is the sum of the manhattan distances
	between each square's current and final position. 
	The optimal path is built and stored in 'solution' */
    public boolean findSolution(boolean hash) {
	this.hash = hash;
	solution.clear();
	Collection<SearchNode> closedSet = new HashSet<SearchNode>();
	PriorityQueue<SearchNode> openSet = new PriorityQueue<SearchNode>();
	Collection<SearchNode> openSetHash = new HashSet<SearchNode>();
	HashMap<SearchNode, SearchNode> pathStore = new HashMap<SearchNode, SearchNode>();
	SearchNode start = new SearchNode(copyBoard(board), emptyP);
	start.gScore = 0;
	start.fScore = start.heuristicCost();
	openSet.add(start);
	boolean inOpen, inClosed;
	int numNodes = 0;
	while(openSet.size() > 0 && openSet.size() < 30000 && !cancel) {
	    SearchNode curr = openSet.poll();
	    openSetHash.remove(curr);
	    numNodes += 1;
	    if(curr.isSolved()) {
		solution = constructPath(pathStore, curr);
		return true;
	    }
	    closedSet.add(curr);
	    for(SearchNode n : getNeighbors(curr)) {
		inOpen = openSetHash.contains(n);
		int newG = curr.gScore + 1;
		inClosed = closedSet.contains(n);
		if((inClosed || inOpen) && newG > n.gScore) {
		    continue;
		}
		n.gScore = newG;
		n.fScore = newG + n.heuristicCost();
		pathStore.put(n, curr);
		if(!inOpen) {
		    openSet.add(n);
		    openSetHash.add(n);
		}
		if(inClosed)
		    closedSet.remove(n);
	    }
	}
	return false;
    }

    /** Returns a copy of the input board. */
    private int[][] copyBoard(int[][] board) {
	int [][] newBoard = new int[board.length][];
	for(int i = 0; i < board.length; i++)
	    newBoard[i] = board[i].clone();
	return newBoard;
    }

    /** Constructs a path given a HashMap.
	Each key is a SearchNode and the value is its parent in the search tree. */
    private ArrayList<Move> constructPath(HashMap<SearchNode, SearchNode> pathStore, SearchNode curr) {
	ArrayList<Move> path = new ArrayList<Move>();
	SearchNode prev = pathStore.get(curr);
	if(prev == null)
	    return path;
	Point p1, p2;
	while(prev != null) {
	    p1 = prev.emptyP;
	    p2 = curr.emptyP;
	    path.add(0, new Move(p1, p2));
	    curr = prev;
	    prev = pathStore.get(prev);
	}
	return path;
    }

    /** Takes a SearchNode and returns an ArrayList of its neighbors. */
    private ArrayList<SearchNode> getNeighbors(SearchNode n) {
	ArrayList<SearchNode> nodes = new ArrayList<SearchNode>();
	int eX = n.emptyP.pX, eY = n.emptyP.pY;
	int[][] newBoard;
	if(eX - 1 >= 0) {
	    newBoard = copyBoard(n.board);
	    newBoard[eX - 1][eY] = n.board[eX][eY];
	    newBoard[eX][eY] = n.board[eX - 1][eY];
	    nodes.add(new SearchNode(newBoard, new Point(eX - 1, eY)));
	}
	if(eX + 1 < dim) {
	    newBoard = copyBoard(n.board);
	    newBoard[eX + 1][eY] = n.board[eX][eY];
	    newBoard[eX][eY] = n.board[eX + 1][eY];
	    nodes.add(new SearchNode(newBoard, new Point(eX + 1, eY)));
	}
	if(eY - 1 >= 0) {
	    newBoard = copyBoard(n.board);
	    newBoard[eX][eY - 1] = n.board[eX][eY];
	    newBoard[eX][eY] = n.board[eX][eY - 1];
	    nodes.add(new SearchNode(newBoard, new Point(eX, eY - 1)));
	}
	if(eY + 1 < dim) {
	    newBoard = copyBoard(n.board);
	    newBoard[eX][eY + 1] = n.board[eX][eY];
	    newBoard[eX][eY] = n.board[eX][eY + 1];
	    nodes.add(new SearchNode(newBoard, new Point(eX, eY + 1)));
	}
	return nodes;
    }

    class SearchNode implements Comparable<SearchNode> {

	int[][] board;
	int gScore=0, fScore=0;
	Point emptyP;

	public SearchNode(int[][] board, Point emptyP) {
	    this.board = board;
	    this.emptyP = emptyP;
	}

	@Override
	public boolean equals(Object other) {
	    SearchNode o = (SearchNode) other;
	    for(int i=0;i<board.length;i+=1) {
		for(int j=0;j<board.length;j+=1) {
		    if(board[i][j] != o.board[i][j]) {
			return false;
		    }
		}
	    }
	    return true;
	}

	@Override
	public int hashCode() {
	    int temp = 0;
	    temp = emptyP.pX * dim + emptyP.pY;
	    for(int i=0;i<dim;i+=1) {
		temp *= board[0][i];
		temp *= board[1][i];
	    }
	    return temp;
	}

	public int compareTo(SearchNode other) {
	    if(fScore > other.fScore) {
		return 1;
	    } else if(fScore < other.fScore) {
		return -1;
	    } else {
		return 0;
	    }
	}

	public int heuristicCost() {
	    int total = 0, finalX, finalY;
	    for(int i=0;i<dim;i+=1) {
		for(int j=0;j<dim;j+=1) {
		    if(board[i][j] == -1) {
			continue;
		    } else {
			finalX = (board[i][j] - 1) % dim;
			finalY = (board[i][j] - 1) / dim;
		    }
		    total += manhattanDist(i, j, finalX, finalY);
		}
	    }
	    return total;
	}

	private int manhattanDist(int x1, int y1, int x2, int y2) {
	    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}

	public boolean isSolved() {
	    int finalX, finalY;
	    for(int i=0;i<dim;i+=1) {
		for(int j=0;j<dim;j+=1) {
		    if(board[i][j] == -1) continue;
		    finalX = (board[i][j] - 1) % dim;
		    finalY = (board[i][j] - 1) / dim;
		    if(finalX != i || finalY != j)
			return false;
		}
	    }
	    return true;
	}

	public String toString() {
	    String ret = "";
	    for(int i=0;i<board.length;i+=1) {
		ret += "( ";
		for(int j=0;j<board.length;j+=1) {
		    ret += board[i][j] + " ";
		}
		ret += ") ";
	    }
	    return ret;
	}

    }

    /* The move class stores a game action.
       'from' is where the empty space was before the move.
       'to' is where the empty space is after the move. */
    public class Move {

	Point from, to;

	public Move(Point from, Point to) {
	    this.from = from;
	    this.to = to;
	}

	public String toString() {
	    return from + " -> " + to;
	}
    }

    public class Point {

	int pX, pY;

	public Point(int x, int y) {
	    this.pX = x;
	    this.pY = y;
	}

	public Point clone() {
	    return new Point(pX, pY);
	}

	public String toString() {
	    return "(" + pX + ", " + pY + ")";
	}

	public Point add(Point p) {
	    return new Point(pX + p.pX, pY + p.pY);
	}

	public boolean equals(Point p) {
	    return pX == p.pX && pY == p.pY;
	}
    }

}