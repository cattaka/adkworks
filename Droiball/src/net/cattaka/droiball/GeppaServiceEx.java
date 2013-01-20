
package net.cattaka.droiball;

import net.cattaka.droiball.data.MyPacket;
import net.cattaka.droiball.data.MyPacketFactory;
import net.cattaka.libgeppa.GeppaService;

public class GeppaServiceEx extends GeppaService<MyPacket> {
    public GeppaServiceEx() {
        super("GeppaSample", new MyPacketFactory());
    }
}
