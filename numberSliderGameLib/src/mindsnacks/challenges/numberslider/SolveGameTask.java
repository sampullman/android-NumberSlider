package com.threeDBJ.numberSlider;

import com.threeDBJ.numberSlider.Game.Move;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class SolveGameTask extends AsyncTask<String,Integer,Boolean> {

    final numberSliderActivity context;
    ProgressDialog d;

    public SolveGameTask(final numberSliderActivity context) {
	this.context = context;
	d = new ProgressDialog(context);
	d.setMessage("Solving...");

	d.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
                @Override
	        public void onClick(DialogInterface dialog, int which) {
		    context.game.cancel(true);
		}
            });
	if(context.gameDim == 4) d.show();
    }

    protected Boolean doInBackground (String... data) {
	return context.game.findSolution(false);
    }

    protected void onPostExecute (Boolean solved) {
	if(!context.game.cancel && !solved) {
	    context.showDialog("Error!", "Sorry, the puzzle was too tough for the solver.");
	}
	context.game.cancel(false);
	if(d != null) d.dismiss();
	if(solved && context.game.solution.size() > 0) {
	    Move m = context.game.solution.get(0);
	    context.performMove(m.to.pX, m.to.pY, false, false, 0);
	    if(context.gameDim == 3)
		context.opt.setText("0");
	    context.game.scrambled = false;
	}
    }

}