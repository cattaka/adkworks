
package net.cattaka.android.foxkehrobo.fragment;

import net.cattaka.android.foxkehrobo.core.IAppStub;
import net.cattaka.android.foxkehrobo.core.MyPreference;
import net.cattaka.android.foxkehrobo.core.ServiceWrapper;
import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.db.FoxkehRoboDatabase;
import net.cattaka.libgeppa.adapter.IDeviceAdapterListener;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import android.content.Context;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment implements IDeviceAdapterListener<FrPacket> {
    protected Context getContext() {
        return getActivity();
    }

    protected FoxkehRoboDatabase getDroiballDatabase() {
        return ((IAppStub)getActivity()).getDroiballDatabase();
    }

    protected IAppStub getAppStub() {
        return (IAppStub)getActivity();
    }

    protected ServiceWrapper getServiceWrapper() {
        return getAppStub().getServiceWrapper();
    }

    public MyPreference getMyPreference() {
        return getAppStub().getMyPreference();
    }

    /** IAppStub */
    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack) {
        getAppStub().replacePrimaryFragment(fragment, withBackStack);
    }

    public void onServiceConnected(ServiceWrapper serviceWrapper) {
    }

    /** IDeviceAdapterListener */
    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {

    }

    /** IDeviceAdapterListener */
    @Override
    public void onReceivePacket(FrPacket packet) {

    }

    public void onPageSelected() {
    }

    public void onPageDeselected() {
    }

}
