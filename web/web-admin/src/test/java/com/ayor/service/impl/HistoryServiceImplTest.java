package com.ayor.service.impl;

import com.ayor.entity.pojo.History;
import com.ayor.entity.vo.HistoryVO;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class HistoryServiceImplTest {

    @Test
    void shouldReturnHistoryWhenRecordExists() {
        HistoryServiceImpl service = new HistoryServiceImpl() {
            @Override
            public History getById(Serializable id) {
                return new History(4, 10, 12, null);
            }
        };

        HistoryVO result = service.getHistoryById(4);

        assertNotNull(result);
        assertEquals(10, result.getThreadId());
    }

    @Test
    void shouldPopulateCreateTimeWhenCreatingHistory() {
        HistoryServiceImpl service = new HistoryServiceImpl() {
            @Override
            public boolean save(History entity) {
                assertNotNull(entity.getCreateTime());
                return true;
            }
        };

        History history = new History();
        history.setThreadId(3);
        history.setAccountId(5);

        assertNull(service.createHistory(history));
    }
}
