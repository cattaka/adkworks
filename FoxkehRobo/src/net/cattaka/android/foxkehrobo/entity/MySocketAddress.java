
package net.cattaka.android.foxkehrobo.entity;

import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.util.gendbhandler.Attribute;
import net.cattaka.util.gendbhandler.GenDbHandler;

@GenDbHandler(find = ":id")
public class MySocketAddress {
    @Attribute(primaryKey = true)
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
