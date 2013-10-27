
package net.cattaka.android.foxkehrobo.core;

import net.cattaka.android.foxkehrobo.data.FaceDetectionAlgorism;
import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.db.FoxkehRoboDatabase;
import net.cattaka.android.foxkehrobo.opencv.DetectionBasedTracker;
import net.cattaka.libgeppa.adapter.IDeviceAdapterListener;
import android.support.v4.app.Fragment;

public interface IAppStub {
    public ServiceWrapper getServiceWrapper();

    public FoxkehRoboDatabase getDroiballDatabase();

    public MyPreference getMyPreference();

    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack);

    public boolean registerDeviceAdapterListener(IDeviceAdapterListener<FrPacket> listener);

    public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener<FrPacket> listener);

    public void setKeepScreen(boolean flag);

    public DetectionBasedTracker getNativeDetector();

    public void loadCascade(FaceDetectionAlgorism algorism);
}
