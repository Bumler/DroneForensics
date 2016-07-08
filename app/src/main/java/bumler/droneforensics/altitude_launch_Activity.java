package bumler.droneforensics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by d10 on 7/7/2016.
 */

public class altitude_launch_Activity extends Activity {
    private int altitude = 0;
    private Button toEx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.altitude_launch);

        toExecute();
    }

    private void toExecute(){
        toEx = (Button) findViewById(R.id.toExecute);
        toEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent i = new Intent(altitude_launch_Activity.this, execute_launch_Activity.class);
                    startActivity(i);
            }
        });
    }
}
