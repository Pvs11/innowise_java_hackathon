package cryptocurrencyBotApplication.exceptions;


public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException() {
        super("Basic URL not found! Check it!");
    }
}
