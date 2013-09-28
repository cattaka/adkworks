
package net.cattaka.android.foxkehrobo.entity;

import java.io.Serializable;

import net.cattaka.util.gendbhandler.Attribute;
import net.cattaka.util.gendbhandler.Attribute.FieldType;
import net.cattaka.util.gendbhandler.GenDbHandler;

@GenDbHandler(find = {
        "id", "actionId:sort+"
}, unique = {
    "actionId,sort"
})
public class PoseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class ShortCorder {
        public static Short encode(Byte arg) {
            return (arg != null) ? arg.shortValue() : null;
        }

        public static Byte decode(Short arg) {
            return (arg != null) ? arg.byteValue() : null;
        }
    }

    public static class BooleanCorder {
        public static Short encode(Boolean arg) {
            return (arg != null) ? (arg ? (short)1 : (short)0) : null;
        }

        public static Boolean decode(Short arg) {
            return (arg != null) ? (arg != 0) : null;
        }
    }

    @Attribute(primaryKey = true)
    private Long id;

    private Long actionId;

    private Long sort;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte headYaw;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte headPitch;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte armLeft;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte armRight;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte footLeft;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte footRight;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte earLeft;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte earRight;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte tailYaw;

    @Attribute(customDataType = FieldType.SHORT, customCoder = ShortCorder.class)
    private Byte tailPitch;

    private Integer time;

    public PoseModel() {
    }

    public PoseModel(PoseModel src) {
        set(src);
    }

    public void setNonKeyValues(Byte headYaw, Byte headPitch, Byte armLeft, Byte armRight,
            Byte footLeft, Byte footRight, Byte earLeft, Byte earRight, Byte tailYaw,
            Byte tailPitch, Integer time) {
        this.headYaw = headYaw;
        this.headPitch = headPitch;
        this.armLeft = armLeft;
        this.armRight = armRight;
        this.footLeft = footLeft;
        this.footRight = footRight;
        this.earLeft = earLeft;
        this.earRight = earRight;
        this.tailYaw = tailYaw;
        this.tailPitch = tailPitch;
        this.time = time;
    }

    public void makeStandPose() {
        this.headYaw = 0x7F;
        this.headPitch = 0x7F;
        this.armLeft = 0x7F;
        this.armRight = 0x7F;
        this.footLeft = 0x7F;
        this.footRight = 0x7F;
        this.earLeft = 0x7F;
        this.earRight = 0x7F;
        this.tailYaw = 0x7F;
        this.tailPitch = 0x7F;
        this.time = 1000;
    }

    public void set(PoseModel src) {
        this.id = src.id;
        this.actionId = src.actionId;
        this.sort = src.sort;
        this.headYaw = src.headYaw;
        this.headPitch = src.headPitch;
        this.armLeft = src.armLeft;
        this.armRight = src.armRight;
        this.footLeft = src.footLeft;
        this.footRight = src.footRight;
        this.earLeft = src.earLeft;
        this.earRight = src.earRight;
        this.tailYaw = src.tailYaw;
        this.tailPitch = src.tailPitch;
        this.time = src.time;
    }

    public byte[] toServoAngles() {
        byte[] data = new byte[] {
                (byte)(0xFF & armLeft), //
                (byte)(0xFF & armRight), //
                (byte)(0xFF & footLeft), //
                (byte)(0xFF & footRight), //
                (byte)(0xFF & headYaw), //
                (byte)(0xFF & headPitch), //
                (byte)(0xFF & earLeft), //
                (byte)(0xFF & earRight), //
                (byte)(0xFF & tailYaw), //
                (byte)(0xFF & tailPitch), //
        //
        };
        return data;
    }

    public byte[] toPose() {
        int flags = //
        ((armLeft != null) ? (1 << 0) : 0) //
                | ((armRight != null) ? (1 << 1) : 0) //
                | ((footLeft != null) ? (1 << 2) : 0) //
                | ((footRight != null) ? (1 << 3) : 0) //
                | ((headYaw != null) ? (1 << 4) : 0) //
                | ((headPitch != null) ? (1 << 5) : 0) //
                | ((tailYaw != null) ? (1 << 6) : 0) //
                | ((tailPitch != null) ? (1 << 7) : 0) //
                | ((earLeft != null) ? (1 << 8) : 0) //
                | ((earRight != null) ? (1 << 9) : 0); //
        byte[] data = new byte[] {
                (byte)(0xFF & flags), //
                (byte)(0xFF & (flags >> 8)), //
                (byte)(0xFF & toPrimitive(armLeft)), //
                (byte)(0xFF & toPrimitive(armRight)), //
                (byte)(0xFF & toPrimitive(footLeft)), //
                (byte)(0xFF & toPrimitive(footRight)), //
                (byte)(0xFF & toPrimitive(headYaw)), //
                (byte)(0xFF & toPrimitive(headPitch)), //
                (byte)(0xFF & toPrimitive(tailYaw)), //
                (byte)(0xFF & toPrimitive(tailPitch)), //
                (byte)(0xFF & toPrimitive(earLeft)), //
                (byte)(0xFF & toPrimitive(earRight)), //

        //
        //
        };
        return data;
    }

    private byte toPrimitive(Byte b) {
        return (b != null) ? b : 0;
    }

    // private boolean toPrimitive(Boolean b) {
    // return (b != null && b) ? b : false;
    // }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public Byte getHeadYaw() {
        return headYaw;
    }

    public void setHeadYaw(Byte headYaw) {
        this.headYaw = headYaw;
    }

    public Byte getHeadPitch() {
        return headPitch;
    }

    public void setHeadPitch(Byte headPitch) {
        this.headPitch = headPitch;
    }

    public Byte getArmLeft() {
        return armLeft;
    }

    public void setArmLeft(Byte armLeft) {
        this.armLeft = armLeft;
    }

    public Byte getArmRight() {
        return armRight;
    }

    public void setArmRight(Byte armRight) {
        this.armRight = armRight;
    }

    public Byte getFootLeft() {
        return footLeft;
    }

    public void setFootLeft(Byte footLeft) {
        this.footLeft = footLeft;
    }

    public Byte getFootRight() {
        return footRight;
    }

    public void setFootRight(Byte footRight) {
        this.footRight = footRight;
    }

    public Byte getEarLeft() {
        return earLeft;
    }

    public void setEarLeft(Byte earLeft) {
        this.earLeft = earLeft;
    }

    public Byte getEarRight() {
        return earRight;
    }

    public void setEarRight(Byte earRight) {
        this.earRight = earRight;
    }

    public Byte getTailYaw() {
        return tailYaw;
    }

    public void setTailYaw(Byte tailYaw) {
        this.tailYaw = tailYaw;
    }

    public Byte getTailPitch() {
        return tailPitch;
    }

    public void setTailPitch(Byte tailPitch) {
        this.tailPitch = tailPitch;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

}
