package bumler.droneforensics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by d10 on 7/7/2016.
 */
public class location_launch_Activity extends Activity{

    private int boundaries = 0;
    private ImageButton setLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_launch);

        setLocation();
    }

    private void setLocation(){
        setLoc = (ImageButton) findViewById(R.id.button);
        setLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boundaries++;
                if (boundaries <= 5) {
                    Intent i = new Intent(location_launch_Activity.this, altitude_launch_Activity.class);
                    startActivity(i);
                }
            }
        });
    }
}
