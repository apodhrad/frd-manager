package frdext;

public class FrdException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FrdException() {
		super();
	}

	public FrdException(String message, Throwable cause) {
		super(message, cause);
	}

	public FrdException(String message) {
		super(message);
	}

	public FrdException(Throwable cause) {
		super(cause);
	}

}
