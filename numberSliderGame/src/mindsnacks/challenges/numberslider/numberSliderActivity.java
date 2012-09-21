package mindsnacks.challenges.numberslider;

import com.google.ads.*;

import mindsnacks.challenges.numberslider.Game.Point;
import mindsnacks.challenges.numberslider.Game.Move;

import android.app.Activity;
import android.os.Bundle;

import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.content.Context;
import android.os.AsyncTask;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;

import android.support.v7.widget.GridLayout;

import java.util.ArrayList;

import android.util.Log;

public class numberSliderActivity extends Activity {

    Game game;
    GameButton[][] board;
    int gameDim=3;

    Button toggle;
    TextView opt, opt_text, move_num;
    GridLayout gameBoard;

    int translateX, translateY, moves=0, orientation;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	Configuration config = this.getResources().getConfiguration();
	this.orientation = config.orientation;
	if(orientation == 1) {
            setContentView(R.layout.main);
	} else if(orientation == 2) {
            setContentView(R.layout.main_wide);
	}
	setupUI();
	// Initialize game and draw the board
	game = new Game(gameDim);
	board = new GameButton[gameDim][gameDim];
	setupBoard();
	if(gameDim == 3) {
	    game.findSolution(false);
	    opt.setText(Integer.toString(game.solution.size()));
	}
	setMoves(0);
    }

    /** Handles a screen orientation change */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
	this.orientation = config.orientation;
        if(orientation == 1) {
            setContentView(R.layout.main);
        } else if(orientation == 2) {
            setContentView(R.layout.main_wide);
	}
	setupUI();
	setupBoard();
	if(gameDim == 3) {
	    toggle.setText("4x4");
	    opt_text.setText("Optimal: ");
	    opt.setText(Integer.toString(game.solution.size()));
	} else {
	    opt_text.setText("");
	    toggle.setText("3x3");
	}
	move_num.setText(Integer.toString(moves));
    }

    public void setupUI() {
	// Save references to some views for later
	opt_text = (TextView) findViewById(R.id.optimal_text);
	opt = (TextView) findViewById(R.id.optimal);
	gameBoard = (GridLayout) findViewById(R.id.game_board);
	move_num = (TextView) findViewById(R.id.moves);
	toggle = (Button) findViewById(R.id.toggle);

	// Set up click handlers
	Button b = (Button) findViewById(R.id.undo);
	b.setOnClickListener(undoListener);
	b = (Button) findViewById(R.id.solve);
	b.setOnClickListener(solveListener);
	b = (Button) findViewById(R.id.scramble);
	b.setOnClickListener(scrambleListener);
	toggle.setOnClickListener(toggleListener);
    }

    /** Un-does the last user move and decrements the move counter. */
    private OnClickListener undoListener = new OnClickListener() {
	    public void onClick(View v) {
		if(game.userMoves.size() > 0) {
		    Move m = game.userMoves.remove(game.userMoves.size() - 1);
		    performMove(m.from.pX, m.from.pY, false, true, -1);
		}
	    }
	};

    /** Solves the game in the background. A 3x3 game can almost always
	be solved in < 1 second, but a 4x4 game can cause an out of memory
	exception, so it times out after ~20 seconds if no solution is found. */
    private OnClickListener solveListener = new OnClickListener() {
	    public void onClick(View v) {
		new SolveGameTask(numberSliderActivity.this).execute();
	    }
	};

    /** Scrambles the board, resets the move count, and calculates the
	optimal number of moves (if it's a 3x3 game). */
    private OnClickListener scrambleListener = new OnClickListener() {
	    public void onClick(View v) {
		GameButton b;
		game.scrambleBoard();
		for(int i=0;i<gameDim;i+=1) {
		    for(int j=0;j<gameDim;j+=1) {
			b = board[i][j];
			if(game.board[i][j] == -1)
			    b.setVisibility(View.INVISIBLE);
			else
			    b.setVisibility(View.VISIBLE);
			b.setText(Integer.toString(game.board[i][j]));
		    }
		}
		setMoves(0);
		if(gameDim == 3) {
		    game.findSolution(false);
		    opt.setText(Integer.toString(game.solution.size()));
		}
	    }
	};

    /** Toggles the game between 3x3 and 4x4 mode */
    private OnClickListener toggleListener = new OnClickListener() {
	    public void onClick(View v) {
		if(gameDim == 3) {
		    gameDim = 4;
		    toggle.setText("3x3");
		    opt_text.setText("");
		    opt.setText("");
		} else {
		    gameDim = 3;
		    toggle.setText("4x4");
		    opt_text.setText("Optimal: ");
		}
		game = new Game(gameDim);
		board = new GameButton[gameDim][gameDim];
		gameBoard.removeAllViews();
		setupBoard();
		if(gameDim == 3) {
		    game.findSolution(false);
		    opt.setText(Integer.toString(game.solution.size()));
		}
		setMoves(0);
	    }
	};

    /** Shows a cancelable dialog to the user. */
    public void showDialog(String title, String text) {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog);
        d.setTitle(title);
        TextView t = (TextView) d.findViewById(R.id.text);
        t.setText(text);
        d.setCancelable(true);
        Button done = (Button) d.findViewById(R.id.done);
        done.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    d.dismiss();
		}
	    }
	    );
        d.show();
    }


    /** Set the move count TextView to n */
    public void setMoves(int n) {
	moves = n;
	move_num.setText(Integer.toString(moves));
    }

    /** Determines the amount of translation to move a view one square
	over. Called when a GameButton is drawn. */
    public void setTranslate(int x, int y) {
	translateX = x;
	translateY = y;
    }

    /** Sets up the screen to display the current Game.board */
    public void setupBoard() {
	ViewGroup row;
	GameButton b;
	Display d = getWindowManager().getDefaultDisplay();
	int width, height;
	if(orientation == 1) {
	    width = (d.getWidth() - (20 + gameDim * 5)) / gameDim;
	    height = ((d.getHeight() * 11) / 20) / gameDim;
	} else {
	    width = (((d.getWidth() * 7) / 10) - (40 + gameDim * 5)) / gameDim;
	    height = (((d.getHeight() * 4) / 5) - 10) / gameDim;
	}
	GridLayout.Spec specX, specY;
	GridLayout.LayoutParams params;
	gameBoard.setColumnCount(gameDim);
	gameBoard.setRowCount(gameDim);
	gameBoard.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
	LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	for(int i=0;i<gameDim;i+=1) {
	    for(int j=0;j<gameDim;j+=1) {
		specX = GridLayout.spec(j);
		specY = GridLayout.spec(i);
		params = new GridLayout.LayoutParams(specX, specY);
		params.width = width;
		params.height = height;
		b = (GameButton) inflater.inflate(R.layout.board_cell, gameBoard, false);
		b.setLayoutParams(params);
		if(game.board[i][j] == -1) {
		    b.setVisibility(View.INVISIBLE);
		}
		b.setText(Integer.toString(game.board[i][j]));
		b.setOnClickListener(makeGameClickListener(i, j));
		b.setActivity(this);
		board[i][j] = b;
		gameBoard.addView(b, j);
	    }
	}
    }

    /** Performs a move corresponding to a user click */
    public OnClickListener makeGameClickListener(final int x, final int y) {
	return new OnClickListener() {
	    public void onClick(View v) {
		performMove(x, y, true, false, -1);
		if(game.isSolved() && game.scrambled) {
		    if(gameDim == 3) {
			if(game.solution.size() == moves) {
			    showDialog("Victory!", "Outstanding! you solved the puzzle " +
				       "in the optimal number of moves!");
			} else {
			    showDialog("Good job!", "Victory! You solved the puzzle in " +
				       (moves - game.solution.size()) +
				       " more moves than the optimal solution.");
			}
			opt.setText("0");
		    } else {
			showDialog("Good job!", "You've solved the puzzle!");
		    }
		    game.scrambled = false;
		    setMoves(0);
		}
	    }
	};
    }

    /** Performs a move corresponding to a click at the position (x, y) on the game
	grid, where x is the column and y is the row. 
	    -Stores the move when it is a regular user click.
	    -Increments or decrements the move count acorrding to the undo param
	    -If seqNum is set, the solution is being executed. After each animation completes
	        the next move in the sequence will occur. */
    public void performMove(int x, int y, boolean saveMove, boolean undo, int seqNum) {
	Log.v("numberSlide", "clicked");
	if(game.isLegalMove(x, y)) {
	    Point dir;
	    int xOffset=0, yOffset=0;
	    // Set up animation parameters
	    if(x != game.emptyP.pX) {
		if(x < game.emptyP.pX) {
		    dir = game.new Point(-1, 0);
		    xOffset = translateX;
		} else {
		    dir = game.new Point(1, 0);
		    xOffset = -1 * translateX;
		}
	    } else {
		if(y < game.emptyP.pY) {
		    dir = game.new Point(0, -1);
		    yOffset = translateY;
		} else {
		    dir = game.new Point(0, 1);
		    yOffset = -1 * translateY;
		}
	    }
	    // Animate the move
	    TranslateAnimation slide;
	    Point p = game.emptyP.clone();
	    Point endP = game.new Point(x, y), nextP;
	    GameButton b = board[p.pX][p.pY], nextB;
	    GameButton newEmptyB = b;
	    ViewGroup row, gameBoard = (ViewGroup) findViewById(R.id.game_board);
	    boolean empty = false;
	    String newText;
	    int moveDiff = undo ? -1 : 1;
	    while(true) {
		moves += moveDiff;
		nextP = p.add(dir);
		if(nextP.equals(endP)) {
		    empty = true;
		}
		slide = new TranslateAnimation(0, xOffset, 0, yOffset);
		slide.setDuration(250);
		nextB = board[nextP.pX][nextP.pY];
		newText = nextB.getText().toString();
		slide.setAnimationListener(makeAnimationListener(nextB, b, newText, empty, seqNum));
		nextB.startAnimation(slide);
		p = nextP;
		b = nextB;
		if(empty) break;
	    }
	    game.performMove(x, y, saveMove);
	    TextView m = (TextView) findViewById(R.id.moves);
	    m.setText(Integer.toString(moves));
	} else {
	    Log.v("click", "illegal move: " + x + ", " + y);
	}
    }

    public AnimationListener makeAnimationListener(final GameButton prev, final GameButton curr,
						   final String newText, final boolean empty,
						   final int seqNum) {
	return new AnimationListener() {
	    public void onAnimationStart(Animation arg0) {
	    }

	    public void onAnimationRepeat(Animation arg0) {
	    }

	    public void onAnimationEnd(Animation arg0) {
		curr.setText(newText);
		prev.setAnimation(null);
		curr.setVisibility(View.VISIBLE);
		curr.setClickable(true);
		if (empty) {
		    prev.setVisibility(View.INVISIBLE);
		    prev.setClickable(false);
		    if(seqNum != -1 && seqNum + 1 < game.solution.size()) {
			Move m = game.solution.get(seqNum + 1);
			performMove(m.to.pX, m.to.pY, false, false, seqNum + 1);
		    }
		}
	    }
        };
    }
}
