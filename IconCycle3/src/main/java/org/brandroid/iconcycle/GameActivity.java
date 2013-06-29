package org.brandroid.iconcycle;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

import org.brandroid.iconcycle.game.GameHelper;

public class GameActivity extends Activity implements GameHelper.GameHelperListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            // start the asynchronous sign in flow
            //beginUserInitiatedSignIn();
        }
        else if (v.getId() == R.id.sign_out_button) {
            // sign out.
            //signOut();

            // show sign-in button, hide the sign-out button
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }
}
