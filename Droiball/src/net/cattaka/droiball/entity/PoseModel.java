
package net.cattaka.droiball.entity;

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
    private Byte head;

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

    @Attribute(customDataType = FieldType.SHORT, customCoder = BooleanCorder.class)
    private Boolean eyeLeft;

    @Attribute(customDataType = FieldType.SHORT, customCoder = BooleanCorder.class)
    private Boolean eyeRight;

    private Integer time;

    public PoseModel() {
    }

    public PoseModel(PoseModel src) {
        set(src);
    }

    public void setNonKeyValues(Byte head, Byte armLeft, Byte armRight, Byte footLeft,
            Byte footRight, Byte earLeft, Byte earRight, Boolean eyeLeft, Boolean eyeRight,
            Integer time) {
        this.head = head;
        this.armLeft = armLeft;
        this.armRight = armRight;
        this.footLeft = footLeft;
        this.footRight = footRight;
        this.earLeft = earLeft;
        this.earRight = earRight;
        this.eyeLeft = eyeLeft;
        this.eyeRight = eyeRight;
        this.time = time;
    }

    public void makeStandPose() {
        this.head = 0x7F;
        this.armLeft = 0x7F;
        this.armRight = 0x7F;
        this.footLeft = 0x7F;
        this.footRight = 0x7F;
        this.earLeft = 0x7F;
        this.earRight = 0x7F;
        this.eyeLeft = false;
        this.eyeRight = false;
        this.time = 1000;
    }

    public void set(PoseModel src) {
        this.id = src.id;
        this.actionId = src.actionId;
        this.sort = src.sort;
        this.head = src.head;
        this.armLeft = src.armLeft;
        this.armRight = src.armRight;
        this.footLeft = src.footLeft;
        this.footRight = src.footRight;
        this.earLeft = src.earLeft;
        this.earRight = src.earRight;
        this.eyeLeft = src.eyeLeft;
        this.eyeRight = src.eyeRight;
        this.time = src.time;
    }

    public byte[] toServoAngles() {
        byte[] data = new byte[] {
                (byte)(0xFF & armLeft), //
                (byte)(0xFF & armRight), //
                (byte)(0xFF & footLeft), //
                (byte)(0xFF & footRight), //
                (byte)(0xFF & head), //
                (byte)(0xFF & earLeft), //
                (byte)(0xFF & earRight), //
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
                | ((head != null) ? (1 << 4) : 0) //
                | ((earLeft != null) ? (1 << 5) : 0) //
                | ((earRight != null) ? (1 << 6) : 0) //
                | ((eyeLeft != null) ? (1 << 7) : 0) //
                | ((eyeRight != null) ? (1 << 8) : 0);
        byte[] data = new byte[] {
                (byte)(0xFF & flags), //
                (byte)(0xFF & (flags >> 8)), //
                (byte)(0xFF & toPrimitive(armLeft)), //
                (byte)(0xFF & toPrimitive(armRight)), //
                (byte)(0xFF & toPrimitive(footLeft)), //
                (byte)(0xFF & toPrimitive(footRight)), //
                (byte)(0xFF & toPrimitive(head)), //
                (byte)(0xFF & toPrimitive(earLeft)), //
                (byte)(0xFF & toPrimitive(earRight)), //
                (byte)((toPrimitive(eyeLeft) ? 1 : 0) | (toPrimitive(eyeRight) ? 2 : 0))
        //
        //
        };
        return data;
    }

    private byte toPrimitive(Byte b) {
        return (b != null) ? b : 0;
    }

    private boolean toPrimitive(Boolean b) {
        return (b != null && b) ? b : false;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

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

    public Byte getHead() {
        return head;
    }

    public void setHead(Byte head) {
        this.head = head;
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

    public Boolean getEyeLeft() {
        return eyeLeft;
    }

    public void setEyeLeft(Boolean eyeLeft) {
        this.eyeLeft = eyeLeft;
    }

    public Boolean getEyeRight() {
        return eyeRight;
    }

    public void setEyeRight(Boolean eyeRight) {
        this.eyeRight = eyeRight;
    }

}
