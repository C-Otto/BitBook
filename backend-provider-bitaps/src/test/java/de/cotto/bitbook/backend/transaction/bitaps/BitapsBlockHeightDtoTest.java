package de.cotto.bitbook.backend.transaction.bitaps;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BitapsBlockHeightDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String json = "{\"data\":" +
                      " {\"height\": 673466," +
                      " \"hash\": \"xxx\"" +
                      ", \"header\": \"yyy\"," +
                      " \"adjustedTimestamp\": 1615064015" +
                      "}, \"time\": 0.0016}";
        BitapsBlockHeightDto bitapsBlockHeightDto =
                objectMapper.readValue(json, BitapsBlockHeightDto.class);
        assertThat(bitapsBlockHeightDto.getBlockHeight()).isEqualTo(673_466);
    }
}