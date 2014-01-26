package edu.mit.powers.activity;

import android.os.Bundle;
import android.widget.ProgressBar;

import edu.mit.powers.R;

public class ProductionContentDownloadActivity extends PowersView
{
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_content_download);

        progressBar = (ProgressBar) findViewById(R.id.productionContentProgressBar);
        progressBar.setIndeterminate(true);
    }

    @Override
    public void setProgress(double progress) {
        super.setProgress(progress);

        progressBar.setIndeterminate(false);
        progressBar.setProgress((int)(progress*100));
    }
}
