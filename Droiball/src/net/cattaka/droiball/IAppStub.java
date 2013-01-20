
package net.cattaka.droiball;

import net.cattaka.droiball.db.DroiballDatabase;
import net.cattaka.droiball.util.MyPreference;
import net.cattaka.libgeppa.IGeppaService;

import org.opencv.samples.fd.DetectionBasedTracker;

public interface IAppStub {
    public IGeppaService getGeppaService();

    public DroiballDatabase getDroiballDatabase();

    public DetectionBasedTracker getNativeDetector();

    public MyPreference getMyPreference();
}
