package de.cotto.bitbook.backend.transaction.blockchair;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BlockchairBlockHeightDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String json = "{\"data\": {\"blocks\": 673466, \"foo\": \"bar\"}}";
        BlockchairBlockHeightDto blockchairBlockHeightDto =
                objectMapper.readValue(json, BlockchairBlockHeightDto.class);
        assertThat(blockchairBlockHeightDto.getBlockHeight()).isEqualTo(673_466);
    }
}