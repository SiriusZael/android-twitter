package com.codepath.apps.mytwitterclient.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.codepath.apps.mytwitterclient.EndlessScrollListener;
import com.codepath.apps.mytwitterclient.R;
import com.codepath.apps.mytwitterclient.TwitterApplication;
import com.codepath.apps.mytwitterclient.TwitterClient;
import com.codepath.apps.mytwitterclient.adapters.TweetsAdapter;
import com.codepath.apps.mytwitterclient.models.Tweet;
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
    private long sinceId;
    private long maxId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
            sinceId = newTweets.get(0).getUid();
            maxId = newTweets.get(newTweets.size() - 1).getUid() - 1;
            aTweets.addAll(newTweets);
            aTweets.notifyDataSetChanged();
            super.onSuccess(statusCode, headers, response);
        }
    };

    private void populateTimeline() {
        client.getHomeTimeline(responseHandler);
    }

    private void loadMoreData() {
        client.getNextHomeTimeline(maxId, responseHandler);
    }
}
