package com.ithought.rahul.ithought;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AboutApp extends AppCompatActivity {

    private Button shareIdea;
    private TextView wearPink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_about_app);

        shareIdea = (Button)findViewById(R.id.shareIdeas);
        shareIdea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address[] = {"ithoughtapplication@gmail.com"};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));// only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL,address);
                intent.putExtra(Intent.EXTRA_SUBJECT, "iThought User");

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        wearPink = (TextView)findViewById(R.id.wearPink);
        wearPink.setPaintFlags(wearPink.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
