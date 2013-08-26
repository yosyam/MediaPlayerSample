package net.metax.mediaplayersample;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button mButtonPlayer;
    private Button mButtonServicePlayer;
    private Button mButtonChannelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonPlayer = (Button) findViewById(R.id.buttonPlayer);
        mButtonPlayer.setOnClickListener(this);

        mButtonServicePlayer = (Button) findViewById(R.id.buttonServicePlayer);
        mButtonServicePlayer.setOnClickListener(this);

        mButtonChannelList = (Button) findViewById(R.id.buttonChannelList);
        mButtonChannelList.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public void onClick(View view) {
        if ( view == mButtonPlayer ) {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            startActivity(intent);
        } else if ( view == mButtonServicePlayer) {
            Intent intent = new Intent(MainActivity.this, PlayerRemoteControlActivity.class);
            startActivity(intent);
        } else if ( view == mButtonChannelList) {
        Intent intent = new Intent(MainActivity.this, ChannelListActivity.class);
        startActivity(intent);
    }
    }
}
