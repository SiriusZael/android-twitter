package com.codepath.apps.mytwitterclient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.mytwitterclient.R;
import com.codepath.apps.mytwitterclient.models.User;
import com.squareup.picasso.Picasso;

public class ComposeActivity extends AppCompatActivity {
    private User currentUser;
    private ImageView ivProfileImage;
    private TextView tvUserName;
    private TextView tvScreenName;
    private EditText etTweetBody;
    private final int MAX_CHAR_COUNT = 140;
    private int charLeft = MAX_CHAR_COUNT;
    private Menu optMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentUser = getIntent().getParcelableExtra("currentUser");

        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        ivProfileImage.setImageResource(android.R.color.transparent);
        Picasso.with(this).load(currentUser.getProfileImageUrl()).into(ivProfileImage);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserName.setText(currentUser.getName());
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        tvScreenName.setText(getResources().getString(R.string.user_handle, currentUser.getScreenName()));

        etTweetBody = (EditText) findViewById(R.id.etTweetBody);
        etTweetBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charLeft = MAX_CHAR_COUNT - s.length();
                if (optMenu != null) {
                    MenuItem item = optMenu.findItem(R.id.text_char_count);
                    if (charLeft < 0) {
                        item.setTitle("0");
                    } else {
                        item.setTitle(String.valueOf(charLeft));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setIcon(R.drawable.ic_logo_draw);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_draw);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_submit:
                if (charLeft < 140) {
                    if (charLeft >= 0) {
                        Intent in = new Intent();
                        in.putExtra("tweetBody", etTweetBody.getText().toString());
                        setResult(RESULT_OK, in);
                        finish();
                    } else {
                        Toast.makeText(this, "Cannot submit a tweet with more than 140 characters", Toast.LENGTH_SHORT).show();
                    }
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optMenu = menu;
        getMenuInflater().inflate(R.menu.menu_submit_tweet, menu);
        MenuItem item = menu.findItem(R.id.text_char_count);
        item.setTitle(String.valueOf(charLeft));
        return super.onCreateOptionsMenu(menu);
    }
}
