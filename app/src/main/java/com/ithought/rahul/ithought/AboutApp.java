package com.ithought.rahul.ithought;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutApp extends AppCompatActivity {

    private Button shareIdea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
