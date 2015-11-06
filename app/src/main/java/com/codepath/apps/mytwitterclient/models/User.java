package com.codepath.apps.mytwitterclient.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mrucker on 11/6/15.
 */
@Table(name = "users")
public class User extends Model {
    @Column(name = "name") private String name;
    @Column(name = "uid") private long uid;
    @Column(name = "screenName") private String screenName;
    @Column(name = "profileImageUrl") private String profileImageUrl;

    public User() {
        super();
    }

    public static User fromJSON(JSONObject json) {
        User user = new User();

        try {
            user.name = json.getString("name");
            user.uid = json.getLong("id");
            user.screenName = json.getString("screen_name");
            user.profileImageUrl = json.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
