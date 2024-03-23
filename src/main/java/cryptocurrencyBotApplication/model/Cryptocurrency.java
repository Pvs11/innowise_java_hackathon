package cryptocurrencyBotApplication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cryptocurrency {
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("price")
    private double price;

}
