
package net.cattaka.android.humitemp4ble.fragment;

import net.cattaka.android.humitemp4ble.IAppListener;
import net.cattaka.android.humitemp4ble.IAppStub;
import net.cattaka.android.humitemp4ble.db.DbHelper;
import net.cattaka.android.humitemp4ble.util.MyPreference;
import net.cattaka.libgeppa.IPassiveGeppaService;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment implements IAppListener {
    protected IPassiveGeppaService getGeppaService() {
        return ((IAppStub)getActivity()).getGeppaService();
    }

    protected DbHelper getDroiballDatabase() {
        return ((IAppStub)getActivity()).getDroiballDatabase();
    }

    protected IAppStub getAppStub() {
        return (IAppStub)getActivity();
    }

    public MyPreference getMyPreference() {
        return getAppStub().getMyPreference();
    }
}
