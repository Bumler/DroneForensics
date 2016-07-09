package bumler.droneforensics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by d10 on 7/8/2016.
 */

public class practice_practice_Activity extends Activity{
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.practice_practice);

        back();
    }

    public void back(){
        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(practice_practice_Activity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}
