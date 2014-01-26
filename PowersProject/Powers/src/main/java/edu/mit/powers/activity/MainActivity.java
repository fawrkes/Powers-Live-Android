package edu.mit.powers.activity;

import android.os.Bundle;

import edu.mit.powers.R;

public class MainActivity extends PowersView
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
