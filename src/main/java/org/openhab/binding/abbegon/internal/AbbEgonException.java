package org.openhab.binding.abbegon.internal;

/**
 * Created by Ondřej Pečta on 10. 8. 2016.
 */
public class AbbEgonException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AbbEgonException(String message) {
        super(message);
    }

    public AbbEgonException(final Throwable cause) {
        super(cause);
    }

    public AbbEgonException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
