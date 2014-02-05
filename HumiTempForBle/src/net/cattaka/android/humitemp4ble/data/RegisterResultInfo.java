
package net.cattaka.android.humitemp4ble.data;

import net.vvakame.util.jsonpullparser.annotation.JsonKey;
import net.vvakame.util.jsonpullparser.annotation.JsonModel;

@JsonModel
public class RegisterResultInfo {
    @JsonKey
    private boolean result;

    @JsonKey
    private int userId;

    @JsonKey
    private String username;

    @JsonKey
    private String token;

    @JsonKey
    private String message;

    public UserInfo toUserInfo() {
        UserInfo info = new UserInfo();
        info.setUserId(userId);
        info.setUsername(username);
        info.setToken(token);
        return info;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
