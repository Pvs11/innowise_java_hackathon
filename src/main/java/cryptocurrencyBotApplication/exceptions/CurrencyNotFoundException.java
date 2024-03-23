package cryptocurrencyBotApplication.exceptions;

public class CurrencyNotFoundException extends RuntimeException {
    private String message;
    public CurrencyNotFoundException(String msg) {
        this.message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
