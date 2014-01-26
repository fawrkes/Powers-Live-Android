package edu.mit.powers.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import edu.mit.powers.Log;
import edu.mit.powers.R;
import edu.mit.powers.content.cuelist.CueListLocation;

public class WelcomeAndInfoActivity extends PowersView
{
    private int selection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_and_info);

        configureUI();
    }

    public void configureUI()
    {
        // Submit button
        Button submitBtn = (Button) this.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveOnIfSelected();
            }
        });

        // Get venues and populate picker
        final List<String> venues = new ArrayList<>();
        if (application.getVenues() == null) {
            Log.warn("Null venues!");
            return;
        }
        for(CueListLocation loc : application.getVenues()) {
            venues.add(loc.longName);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, R.layout.spinner_item, venues);

        ListView venuePicker = (ListView) this.findViewById(R.id.venuePicker);
        venuePicker.setAdapter(dataAdapter);

        venuePicker.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                // Get venue and set it to settings
                Log.info("Selected %s", venues.get(position));
                selection = position;
            }
        });
    }

    private void moveOnIfSelected()
    {
        Log.info("Clicked button.");
        if (selection >= 0)
        {
            CueListLocation cll = application.getVenues()[selection];
            Log.info("Chose %s.", cll.name);
            application.setVenue(cll);
        }
    }
}
