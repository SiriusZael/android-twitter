package com.codepath.apps.mytwitterclient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.codepath.apps.mytwitterclient.EndlessScrollListener;
import com.codepath.apps.mytwitterclient.R;
import com.codepath.apps.mytwitterclient.TwitterApplication;
import com.codepath.apps.mytwitterclient.TwitterClient;
import com.codepath.apps.mytwitterclient.adapters.TweetsAdapter;
import com.codepath.apps.mytwitterclient.models.Tweet;
import com.codepath.apps.mytwitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private TwitterClient client;
    private TweetsAdapter aTweets;
    private ArrayList<Tweet> tweets;
    private ListView lvTweets;
    private long sinceId = 1;
    private long maxId;
    private final int COMPOSE_TWEET_REQUEST = 1337;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.mipmap.ic_logo);
        currentUser = getIntent().getParcelableExtra("currentUser");

        lvTweets = (ListView) findViewById(R.id.lvTweets);
        tweets = new ArrayList<Tweet>();
        aTweets = new TweetsAdapter(this, tweets);
        lvTweets.setAdapter(aTweets);
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                loadMoreData();
                return true;
            }
        });
        client = TwitterApplication.getRestClient();

        populateTimeline();
    }

    private JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            ArrayList<Tweet> newTweets = Tweet.fromJSONArray(response);
            if (sinceId == 1 || newTweets.get(0).getUid() > sinceId) {
                sinceId = newTweets.get(0).getUid();
            }
            maxId = newTweets.get(newTweets.size() - 1).getUid() - 1;
            aTweets.addAll(newTweets);
            aTweets.notifyDataSetChanged();
            super.onSuccess(statusCode, headers, response);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_compose) {
            Intent in = new Intent(HomeActivity.this, ComposeActivity.class);
            in.putExtra("currentUser", currentUser);
            startActivityForResult(in, COMPOSE_TWEET_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_compose_tweet, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMPOSE_TWEET_REQUEST && resultCode == RESULT_OK) {
            System.out.println(data.getStringExtra("tweetBody"));
        }
    }

    private void populateTimeline() {
        client.getHomeTimeline(responseHandler);
    }

    private void loadMoreData() {
        client.getNextHomeTimeline(maxId, responseHandler);
    }
}
