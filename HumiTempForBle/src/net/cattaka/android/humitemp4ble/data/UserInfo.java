
package net.cattaka.android.humitemp4ble.data;

import net.cattaka.android.humitemp4ble.data.handler.UserInfoHandler;
import net.cattaka.util.gendbhandler.GenDbHandler;
import android.os.Parcel;
import android.os.Parcelable;

@GenDbHandler(genParcelFunc = true, genDbFunc = false)
public class UserInfo implements Parcelable {
    public static final Parcelable.Creator<UserInfo> CREATOR = UserInfoHandler.CREATOR;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserInfoHandler.writeToParcel(this, dest, flags);
    }

    private int userId;

    private String username;

    private String token;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
