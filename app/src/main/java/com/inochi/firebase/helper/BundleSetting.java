package com.inochi.firebase.helper;

import android.content.Context;

import com.inochi.firebase.item.UserItem;
import org.json.JSONException;
import org.json.JSONObject;

public class BundleSetting {
    private Setting setting;
    private Context context;

    public BundleSetting(Context context){
        this.context = context;
        this.setting = new Setting(context);
    }

    private UserItem createUserItem(){
        UserItem userItem = new UserItem();
        userItem.setUserId("");
        userItem.setUserName("");
        userItem.setUserEmail("");
        userItem.setUserPhoto("");
        userItem.setUserDate(0);
        userItem.setAppVer("");
        userItem.setUserType(0);
        userItem.setAppName(context.getPackageName());

        return userItem;
    }

    public UserItem getUserItemLast() {
        String strUserItemLast = setting.getSetting(Constants.Setting.Key.LAST_USER, "");
        UserItem userItem = createUserItem();

        if (!strUserItemLast.isEmpty()){
            try {
                JSONObject jsonObject = new JSONObject(strUserItemLast);

                String userId = "";
                String userName = "";
                String userEmail = "";
                String userPhoto = "";
                String appVer = "";
                String appName = "";
                long userDate = 0;
                int userType = 0;

                if (jsonObject.has("userId")){
                    userId = jsonObject.getString("userId");
                }
                if (jsonObject.has("userName")){
                    userName = jsonObject.getString("userName");
                }
                if (jsonObject.has("userEmail")){
                    userEmail = jsonObject.getString("userEmail");
                }
                if (jsonObject.has("userPhoto")){
                    userPhoto = jsonObject.getString("userPhoto");
                }
                if (jsonObject.has("userDate")){
                    userDate = jsonObject.getLong("userDate");
                }
                if (jsonObject.has("userType")){
                    userType = jsonObject.getInt("userType");
                }
                if (jsonObject.has("appVer")){
                    appVer = jsonObject.getString("appVer");
                }
                if (jsonObject.has("appName")){
                    appName = jsonObject.getString("appName");
                }

                userItem.setUserId(userId);
                userItem.setUserName(userName);
                userItem.setUserEmail(userEmail);
                userItem.setUserDate(userDate);
                userItem.setUserPhoto(userPhoto);
                userItem.setUserType(userType);
                userItem.setAppVer(appVer);
                userItem.setAppName(appName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return userItem;
    }

    public void setUserItemLast(UserItem userItem) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", userItem.getUserId());
            jsonObject.put("userName", userItem.getUserName());
            jsonObject.put("userEmail", userItem.getUserEmail());
            jsonObject.put("userDate", userItem.getUserDate());
            jsonObject.put("userPhoto", userItem.getUserPhoto());
            jsonObject.put("userType", userItem.getUserType());
            jsonObject.put("appVer", userItem.getAppVer());
            jsonObject.put("appName", context.getPackageName());

            String strUserItem = jsonObject.toString();
            setting.setSetting(Constants.Setting.Key.LAST_USER, strUserItem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
