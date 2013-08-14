
package net.cattaka.android.humitemp.fragment;

import net.cattaka.android.humitemp.IAppListener;
import net.cattaka.android.humitemp.IAppStub;
import net.cattaka.android.humitemp.db.DbHelper;
import net.cattaka.android.humitemp.util.MyPreference;
import net.cattaka.libgeppa.IGeppaService;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment implements IAppListener {
    protected IGeppaService getGeppaService() {
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
