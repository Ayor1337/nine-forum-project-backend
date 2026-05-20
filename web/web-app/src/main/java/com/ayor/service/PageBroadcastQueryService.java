package com.ayor.service;

import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.type.PageBroadcastScopeType;

import java.util.List;

public interface PageBroadcastQueryService {

    List<PageBroadcastVO> listActiveBroadcasts(PageBroadcastScopeType scopeType, Integer scopeId);
}
