package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.entity.vo.LikeThreadVO;
import com.ayor.mapper.LikeMapper;
import com.ayor.service.LikeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LikeServiceImpl extends ServiceImpl<LikeMapper, LikeThread> implements LikeService {


    /**
     * 分页查询点赞记录，可按帖子和账号组合过滤。
     */
    @Override
    public PageEntity<LikeThreadVO> getLikes(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId) {
        Page<LikeThread> page = this.lambdaQuery()
                .eq(threadId != null, LikeThread::getThreadId, threadId)
                .eq(accountId != null, LikeThread::getAccountId, accountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public LikeThreadVO getLikeById(Integer likeId) {
        if (likeId == null) {
            return null;
        }
        return toVO(this.getById(likeId));
    }

    @Override
    public String createLike(LikeThread like) {
        if (like == null || like.getThreadId() == null || like.getAccountId() == null) {
            return "点赞记录参数不完整";
        }
        return this.save(like) ? null : "创建点赞失败";
    }

    @Override
    public String updateLike(LikeThread like) {
        if (like == null || like.getLikeId() == null) {
            return "点赞记录不存在";
        }
        LikeThread exist = this.getById(like.getLikeId());
        if (exist == null) {
            return "点赞记录不存在";
        }
        if (like.getAccountId() != null) {
            exist.setAccountId(like.getAccountId());
        }
        if (like.getThreadId() != null) {
            exist.setThreadId(like.getThreadId());
        }
        return this.updateById(exist) ? null : "更新点赞失败";
    }

    /**
     * 删除指定点赞记录。
     */
    @Override
    public String deleteLike(Integer likeId) {
        if (likeId == null) {
            return "点赞记录不存在";
        }
        return this.removeById(likeId) ? null : "删除点赞失败";
    }

    private List<LikeThreadVO> toVOList(List<LikeThread> likes) {
        List<LikeThreadVO> likeThreadVOS = new ArrayList<>();
        for (LikeThread like : likes) {
            likeThreadVOS.add(toVO(like));
        }
        return likeThreadVOS;
    }

    private LikeThreadVO toVO(LikeThread like) {
        if (like == null) {
            return null;
        }
        LikeThreadVO likeThreadVO = new LikeThreadVO();
        BeanUtils.copyProperties(like, likeThreadVO);
        return likeThreadVO;
    }
}
