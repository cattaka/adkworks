
package net.cattaka.droiball.data;

public enum FaceDetectionAlgorism {
    HAARCASCADE_FRONTALFACE_ALT_TREE("haarcascade_frontalface_alt_tree.xml"), //
    HAARCASCADE_FRONTALFACE_ALT("haarcascade_frontalface_alt.xml"), //
    HAARCASCADE_FRONTALFACE_ALT2("haarcascade_frontalface_alt2.xml"), //
    HAARCASCADE_FRONTALFACE_DEFAULT("haarcascade_frontalface_default.xml"), //
    LBPCASCADE_FRONTALFACE("lbpcascade_frontalface.xml"), //
    ;

    public final String filename;

    private FaceDetectionAlgorism(String filename) {
        this.filename = filename;
    }

}
