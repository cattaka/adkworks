
package net.cattaka.android.humitemp4ble.entity;

import java.util.Date;

import net.cattaka.android.humitemp4ble.entity.handler.HumiTempModelHandler;
import net.cattaka.util.gendbhandler.Attribute;
import net.cattaka.util.gendbhandler.GenDbHandler;
import android.os.Parcel;
import android.os.Parcelable;

@GenDbHandler(genDbFunc = true, genParcelFunc = true, find = {
    "sendFlag:id+"
})
public class HumiTempModel implements Parcelable {
    public static final Parcelable.Creator<HumiTempModel> CREATOR = HumiTempModelHandler.CREATOR;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        HumiTempModelHandler.writeToParcel(this, dest, flags);
    }

    @Attribute(primaryKey = true)
    private Long id;

    private Long deviceId;

    private Date date;

    private float humidity;

    private float temperature;

    @Attribute(nullValue = "false")
    private boolean sendFlag = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public boolean getSendFlag() {
        return sendFlag;
    }

    public void setSendFlag(boolean sendFlag) {
        this.sendFlag = sendFlag;
    }

}
