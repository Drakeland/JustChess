package com.drakepitts.justchess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainMenu extends Activity {
    public final static String START_NEW_GAME = "com.drakepitts.justchess.START_NEW_GAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void newGame(View view) {
        Intent intent = new Intent(this, ChessGameActivity.class);
        intent = intent.putExtra(START_NEW_GAME, true);
        startActivity(intent);
    }

    public void continueGame(View view) {
        Intent intent = new Intent(this, ChessGameActivity.class);
        intent = intent.putExtra(START_NEW_GAME, false);
        startActivity(intent);
    }

}
