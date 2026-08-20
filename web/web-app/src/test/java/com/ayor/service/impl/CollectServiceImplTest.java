package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Collect;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.CollectMapper;
import com.ayor.mapper.TagMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserRelationService;
import com.ayor.service.CacheInvalidationService;
import com.ayor.util.TipTapUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private CollectMapper collectMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TipTapUtils tipTapUtils;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private CacheInvalidationService cacheInvalidationService;

    // 测试拒绝收藏当拉黑带有帖子串作者
    @Test
    void shouldRejectCollectWhenBlockedWithThreadAuthor() {
        CollectServiceImpl service = createService();
        Account account = new Account();
        account.setAccountId(5);
        Threadd thread = new Threadd();
        thread.setThreadId(9);
        thread.setAccountId(11);
        thread.setIsDeleted(false);

        when(accountMapper.getAccountById(5)).thenReturn(account);
        when(threaddMapper.selectById(9)).thenReturn(thread);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(true);

        String result = service.insertCollect(5, 9);

        assertEquals("已拉黑，不能收藏", result);
        verify(collectMapper, never()).insert(any(Collect.class));
    }

    // 测试收藏列表转换返回正文中的全部图片URL
    @Test
    void shouldReturnAllImageUrlsInCollectedThread() {
        CollectServiceImpl service = createService(new TipTapUtils());
        Threadd thread = new Threadd();
        thread.setThreadId(9);
        thread.setAccountId(11);
        thread.setContent(imageDocument(8));
        Account account = new Account();
        account.setAccountId(11);
        account.setNickname("author");
        when(accountMapper.getAccountById(11)).thenReturn(account);

        ThreadVO result = ReflectionTestUtils.invokeMethod(service, "toVO", thread);

        assertEquals(expectedImageUrls(8), result.getImageUrls());
    }

    private CollectServiceImpl createService() {
        return createService(tipTapUtils);
    }

    private CollectServiceImpl createService(TipTapUtils tipTapUtils) {
        CollectServiceImpl service = new CollectServiceImpl(
                accountMapper,
                threaddMapper,
                tagMapper,
                tipTapUtils,
                privacyPolicyService,
                userRelationService,
                cacheInvalidationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", collectMapper);
        return service;
    }

    private String imageDocument(int imageCount) {
        StringBuilder builder = new StringBuilder("{\"type\":\"doc\",\"content\":[");
        for (int index = 0; index < imageCount; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"type\":\"image\",\"attrs\":{\"src\":\"https://example.com/")
                    .append(index)
                    .append(".png\"}}");
        }
        return builder.append("]}").toString();
    }

    private List<String> expectedImageUrls(int imageCount) {
        List<String> urls = new ArrayList<>(imageCount);
        for (int index = 0; index < imageCount; index++) {
            urls.add("https://example.com/" + index + ".png");
        }
        return urls;
    }
}
