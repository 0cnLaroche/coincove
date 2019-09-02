package webserver.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.javamoney.moneta.FastMoney;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.math.BigDecimal;

@JsonComponent
public class FastMoneySerializer extends JsonSerializer<FastMoney> {
    @Override
    public void serialize(FastMoney value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("amount", BigDecimal.valueOf(value.getNumber().doubleValueExact()));
        gen.writeStringField("currencyCode", value.getCurrency().getCurrencyCode());
        gen.writeEndObject();
    }
}
