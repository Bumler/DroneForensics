package bumler.droneforensics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by d10 on 7/8/2016.
 */

//this class creates a small popup window that has the user input a custom amount of feet and sends it back to the altitude
public class pop_altitude_Activity extends Activity {

    private Button complete;
    private EditText feet = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_pop_altitude);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //this sets the size of the screen
        int width = (int) (dm.widthPixels * .40);
        int height = (int) (dm.heightPixels * .15);

        getWindow().setLayout(width, height);

        setFeet();
        back();
}
    private void setFeet(){
        feet = (EditText)findViewById(R.id.altitude);

}

    private void back(){
        complete = (Button)findViewById(R.id.complete);
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(pop_altitude_Activity.this, altitude_launch_Activity.class);
                startActivity(i);
                pop_altitude_Activity.this.overridePendingTransition(0, 0);
            }
        });
    }
}
