package com.ayor.service.impl;

import com.ayor.entity.pojo.Tag;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.vo.ThreadTableVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.TagMapper;
import com.ayor.mapper.TopicMapper;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThreadServiceImplTest {

    @Test
    void shouldReturnThreadWhenThreadExists() {
        ThreaddServiceImpl service = new ThreaddServiceImpl(null, null, null) {
            @Override
            public Threadd getById(Serializable id) {
                return new Threadd(9, "帖子", "内容", null, null, 1, 2, 3, 4, 5, 6, 7, false, false, false, false);
            }
        };

        Threadd result = service.getThreadById(9);

        assertNotNull(result);
        assertEquals(9, result.getThreadId());
        assertEquals("帖子", result.getTitle());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFillTagNameWhenConvertingThreadTableVo() throws Exception {
        AccountMapper accountMapper = (AccountMapper) Proxy.newProxyInstance(
                AccountMapper.class.getClassLoader(),
                new Class[]{AccountMapper.class},
                (proxy, method, args) -> "getUsernameById".equals(method.getName()) ? "ayor" : null
        );
        TopicMapper topicMapper = (TopicMapper) Proxy.newProxyInstance(
                TopicMapper.class.getClassLoader(),
                new Class[]{TopicMapper.class},
                (proxy, method, args) -> "getTopicNameById".equals(method.getName()) ? "后端" : null
        );
        TagMapper tagMapper = (TagMapper) Proxy.newProxyInstance(
                TagMapper.class.getClassLoader(),
                new Class[]{TagMapper.class},
                (proxy, method, args) -> {
                    if ("getTagById".equals(method.getName())) {
                        return new Tag(6, "Spring", null, 5);
                    }
                    return null;
                }
        );
        ThreaddServiceImpl service = new ThreaddServiceImpl(accountMapper, topicMapper, tagMapper);
        Method method = ThreaddServiceImpl.class.getDeclaredMethod("toVOList", List.class);
        method.setAccessible(true);

        List<ThreadTableVO> result = (List<ThreadTableVO>) method.invoke(
                service,
                List.of(new Threadd(9, "帖子", "内容", null, null, 1, 2, 3, 4, 5, 6, 7, false, false, false, false))
        );

        assertEquals(1, result.size());
        assertEquals("Spring", result.get(0).getTagName());
        assertEquals("ayor", result.get(0).getAccountName());
        assertEquals("后端", result.get(0).getTopicName());
    }
}
