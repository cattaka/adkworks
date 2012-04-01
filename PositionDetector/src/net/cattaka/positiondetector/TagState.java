package net.cattaka.positiondetector;

public class TagState {
    private int index;
    private TagEvent tagEvent;
    private float[] poseMats;
    
    public TagState() {
        tagEvent = TagEvent.DISAPPEAR;
        poseMats = new float[4*4];
    }
    
    public TagState(int index, TagEvent tagEvent, float[] poseMats) {
        super();
        this.index = index;
        this.tagEvent = tagEvent;
        this.poseMats = new float[4*4];
        System.arraycopy(poseMats, 0, this.poseMats, 0, this.poseMats.length);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public TagEvent getTagEvent() {
        return tagEvent;
    }
    public void setTagEvent(TagEvent tagEvent) {
        this.tagEvent = tagEvent;
    }
    public float[] getPoseMats() {
        return poseMats;
    }
    public void setPoseMats(float[] poseMats) {
        this.poseMats = poseMats;
    }
}
