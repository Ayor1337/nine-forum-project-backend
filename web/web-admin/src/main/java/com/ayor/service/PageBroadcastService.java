package com.ayor.service;

import com.ayor.entity.dto.PageBroadcastDTO;
import com.ayor.entity.vo.PageBroadcastVO;

import java.util.List;

public interface PageBroadcastService {

    String createPageBroadcast(PageBroadcastDTO dto);

    List<PageBroadcastVO> listPageBroadcasts();

    String deletePageBroadcast(String broadcastId);
}
