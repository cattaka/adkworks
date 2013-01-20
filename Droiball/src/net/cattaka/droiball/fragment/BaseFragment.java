
package net.cattaka.droiball.fragment;

import net.cattaka.droiball.IAppListener;
import net.cattaka.droiball.IAppStub;
import net.cattaka.droiball.db.DroiballDatabase;
import net.cattaka.droiball.util.MyPreference;
import net.cattaka.libgeppa.IGeppaService;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment implements IAppListener {
    protected IGeppaService getGeppaService() {
        return ((IAppStub)getActivity()).getGeppaService();
    }

    protected DroiballDatabase getDroiballDatabase() {
        return ((IAppStub)getActivity()).getDroiballDatabase();
    }

    protected IAppStub getAppStub() {
        return (IAppStub)getActivity();
    }

    public MyPreference getMyPreference() {
        return getAppStub().getMyPreference();
    }
}
