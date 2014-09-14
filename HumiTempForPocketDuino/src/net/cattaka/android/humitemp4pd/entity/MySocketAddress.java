
package net.cattaka.android.humitemp4pd.entity;

import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.util.cathandsgendroid.annotation.DataModel;
import net.cattaka.util.cathandsgendroid.annotation.DataModelAttrs;

@DataModel(find = ":id")
public class MySocketAddress {
    @DataModelAttrs(primaryKey = true)
    private Long id;

    private String hostName;

    private Integer port;

    public MySocketAddress() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return hostName + ":" + port;
    }

    public DeviceInfo toDeviceInfo() {
        return DeviceInfo.createTcp(hostName, port, true);
    }
}
