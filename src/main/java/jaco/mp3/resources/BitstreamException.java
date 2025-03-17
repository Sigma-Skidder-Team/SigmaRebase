package jaco.mp3.resources;

public class BitstreamException extends JavaLayerException implements BitstreamErrors {
    private int errorcode;

    public BitstreamException(String msg, Throwable t) {
        super(msg, t);
        this.errorcode = BitstreamErrors.UNKNOWN_ERROR;
    }

    public BitstreamException(int errorcode, Throwable t) {
        this(getErrorString(errorcode), t);
        this.errorcode = errorcode;
    }

    public int getErrorCode() {
        return this.errorcode;
    }

    public static String getErrorString(int errorcode) {
        return "Bitstream errorcode " + Integer.toHexString(errorcode);
    }
}
