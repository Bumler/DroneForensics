package bumler.droneforensics;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

/**
 * Created by d10 on 7/8/2016.
 */
public class pop_altitude_Activity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_pop_altitude);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int) (dm.widthPixels * .25);
        int height = (int) (dm.heightPixels * .25);

        getWindow().setLayout(width, height);
}
}
