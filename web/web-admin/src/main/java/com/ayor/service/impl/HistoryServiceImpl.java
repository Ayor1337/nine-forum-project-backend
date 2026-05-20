package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.History;
import com.ayor.entity.vo.HistoryVO;
import com.ayor.mapper.HistoryMapper;
import com.ayor.service.HistoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, History> implements HistoryService {

    @Override
    public PageEntity<HistoryVO> getHistories(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId) {
        Page<History> page = this.lambdaQuery()
                .eq(threadId != null, History::getThreadId, threadId)
                .eq(accountId != null, History::getAccountId, accountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public HistoryVO getHistoryById(Integer historyId) {
        if (historyId == null) {
            return null;
        }
        return toVO(this.getById(historyId));
    }

    @Override
    public String createHistory(History history) {
        if (history == null || history.getThreadId() == null || history.getAccountId() == null) {
            return "浏览记录参数不完整";
        }
        if (history.getCreateTime() == null) {
            history.setCreateTime(new Date());
        }
        return this.save(history) ? null : "创建浏览记录失败";
    }

    @Override
    public String updateHistory(History history) {
        if (history == null || history.getHistoryId() == null) {
            return "浏览记录不存在";
        }
        History exist = this.getById(history.getHistoryId());
        if (exist == null) {
            return "浏览记录不存在";
        }
        if (history.getThreadId() != null) {
            exist.setThreadId(history.getThreadId());
        }
        if (history.getAccountId() != null) {
            exist.setAccountId(history.getAccountId());
        }
        if (history.getCreateTime() != null) {
            exist.setCreateTime(history.getCreateTime());
        }
        return this.updateById(exist) ? null : "更新浏览记录失败";
    }

    @Override
    public String deleteHistory(Integer historyId) {
        if (historyId == null) {
            return "浏览记录不存在";
        }
        return this.removeById(historyId) ? null : "删除浏览记录失败";
    }

    private List<HistoryVO> toVOList(List<History> histories) {
        List<HistoryVO> historyVOS = new ArrayList<>();
        for (History history : histories) {
            historyVOS.add(toVO(history));
        }
        return historyVOS;
    }

    private HistoryVO toVO(History history) {
        if (history == null) {
            return null;
        }
        HistoryVO historyVO = new HistoryVO();
        BeanUtils.copyProperties(history, historyVO);
        return historyVO;
    }
}
