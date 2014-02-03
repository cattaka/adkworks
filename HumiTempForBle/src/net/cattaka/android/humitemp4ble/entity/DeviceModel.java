
package net.cattaka.android.humitemp4ble.entity;

import java.util.Date;

import net.cattaka.android.humitemp4ble.entity.handler.DeviceModelHandler;
import net.cattaka.util.gendbhandler.Attribute;
import net.cattaka.util.gendbhandler.GenDbHandler;
import android.os.Parcel;
import android.os.Parcelable;

@GenDbHandler(genDbFunc = true, genParcelFunc = true, find = {
        "address", ":address"
}, unique = "address")
public class DeviceModel implements Parcelable {
    public static final Parcelable.Creator<DeviceModel> CREATOR = DeviceModelHandler.CREATOR;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DeviceModelHandler.writeToParcel(this, dest, flags);
    }

    @Attribute(primaryKey = true)
    private Long id;

    private String address;

    private String name;

    private Date lastUpdate;

    private float lastTemplature;

    private float lastHumidity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public float getLastTemplature() {
        return lastTemplature;
    }

    public void setLastTemplature(float lastTemplature) {
        this.lastTemplature = lastTemplature;
    }

    public float getLastHumidity() {
        return lastHumidity;
    }

    public void setLastHumidity(float lastHumidity) {
        this.lastHumidity = lastHumidity;
    }

}
