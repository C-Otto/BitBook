package de.cotto.bitbook.backend.transaction.bitaps;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = BitapsBlockHeightDto.Deserializer.class)
public class BitapsBlockHeightDto {
    private final int blockHeight;

    public BitapsBlockHeightDto(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public static class Deserializer extends JsonDeserializer<BitapsBlockHeightDto> {
        @Override
        public BitapsBlockHeightDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            return new BitapsBlockHeightDto(rootNode.get("data").get("height").intValue());
        }
    }
}
