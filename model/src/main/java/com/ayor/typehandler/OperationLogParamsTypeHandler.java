package com.ayor.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class OperationLogParamsTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize operation log params", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private Map<String, Object> parse(String value) throws SQLException {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            Object parsed = OBJECT_MAPPER.readValue(value, Object.class);
            if (parsed instanceof Map<?, ?>) {
                return OBJECT_MAPPER.convertValue(parsed, MAP_TYPE);
            }
            Map<String, Object> params = new LinkedHashMap<>();
            params.put(parsed instanceof List<?> ? "args" : "value", parsed);
            return params;
        } catch (JsonProcessingException e) {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("raw", value);
            return params;
        } catch (IllegalArgumentException e) {
            throw new SQLException("Failed to convert operation log params", e);
        }
    }
}
