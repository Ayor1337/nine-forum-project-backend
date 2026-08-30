package com.ayor.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyCheckInMapperContractTest {

    @Test
    void shouldRegisterRecentUserAndDailyStatusQueries() throws Exception {
        Configuration configuration = new Configuration();
        try (InputStream inputStream = Resources.getResourceAsStream("mapper/DailyCheckInMapper.xml")) {
            new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    "mapper/DailyCheckInMapper.xml",
                    configuration.getSqlFragments()).parse();
        }

        assertTrue(configuration.hasStatement("com.ayor.mapper.DailyCheckInMapper.selectRecentCheckInUsers"));
        assertTrue(configuration.hasStatement("com.ayor.mapper.DailyCheckInMapper.existsByAccountIdAndCheckInDate"));
    }
}
