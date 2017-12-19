package md.i0.krfam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class ErrorDetected extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_detected);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("KRFAM: Error Detected");
        TextView t = (TextView) findViewById(R.id.errorMessage);
        t.setText("Looks like something broke.\nSorry about that\n\nHeres the error message:\n\n" + KRFAM.errorMessage);
    }
}
