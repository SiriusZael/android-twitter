package com.codepath.apps.mytwitterclient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
    private SwipeRefreshLayout swipeContainer;
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
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        client = TwitterApplication.getRestClient();

        populateTimeline();
    }

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
            client.postTweet(data.getStringExtra("tweetBody"), new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Tweet postedTweet = Tweet.fromJSON(response);
                    aTweets.insert(postedTweet, 0);
                    aTweets.notifyDataSetChanged();
                    super.onSuccess(statusCode, headers, response);
                }
            });
        }
    }

    private void populateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                swipeContainer.setRefreshing(false);

                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                swipeContainer.setRefreshing(false);

                ArrayList<Tweet> newTweets = Tweet.fromJSONArray(response);
                sinceId = newTweets.get(0).getUid();
                maxId = newTweets.get(newTweets.size() - 1).getUid() - 1;
                aTweets.clear();
                aTweets.addAll(newTweets);
                aTweets.notifyDataSetChanged();
                super.onSuccess(statusCode, headers, response);
            }
        });
    }

    private void loadMoreData() {
        client.getNextHomeTimeline(maxId, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                swipeContainer.setRefreshing(false);

                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                swipeContainer.setRefreshing(false);

                ArrayList<Tweet> newTweets = Tweet.fromJSONArray(response);
                if (newTweets.get(0).getUid() > sinceId) {
                    sinceId = newTweets.get(0).getUid();
                }
                maxId = newTweets.get(newTweets.size() - 1).getUid() - 1;
                aTweets.addAll(newTweets);
                aTweets.notifyDataSetChanged();
                super.onSuccess(statusCode, headers, response);
            }
        });
    }
}
