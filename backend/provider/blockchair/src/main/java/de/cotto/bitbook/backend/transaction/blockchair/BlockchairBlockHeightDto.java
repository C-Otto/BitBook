package de.cotto.bitbook.backend.transaction.blockchair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = BlockchairBlockHeightDto.Deserializer.class)
public class BlockchairBlockHeightDto {
    private final int blockHeight;

    public BlockchairBlockHeightDto(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public static class Deserializer extends JsonDeserializer<BlockchairBlockHeightDto> {
        @Override
        public BlockchairBlockHeightDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            return new BlockchairBlockHeightDto(rootNode.get("data").get("blocks").intValue());
        }
    }
}
