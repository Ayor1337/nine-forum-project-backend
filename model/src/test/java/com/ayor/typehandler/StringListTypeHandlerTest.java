package com.ayor.typehandler;

import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StringListTypeHandlerTest {

    private final StringListTypeHandler typeHandler = new StringListTypeHandler();

    @Test
    void readsJsonArrayAndNormalizesNullToEmptyList() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("images_urls")).thenReturn("[\"https://example.com/a.png\"]");

        assertThat(typeHandler.getNullableResult(resultSet, "images_urls"))
                .containsExactly("https://example.com/a.png");

        when(resultSet.getString("images_urls")).thenReturn(null);
        assertThat(typeHandler.getNullableResult(resultSet, "images_urls")).isEmpty();
    }

    @Test
    void writesImageUrlsAsJsonArray() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);

        typeHandler.setNonNullParameter(statement, 1, List.of("https://example.com/a.png"), null);

        verify(statement).setString(1, "[\"https://example.com/a.png\"]");
    }
}
