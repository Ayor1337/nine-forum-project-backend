package com.ayor.mapper;

import com.ayor.entity.pojo.SpotifyToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Spotify Token Mapper
 * 处理Spotify OAuth令牌的数据库操作
 */
@Mapper
public interface SpotifyTokenMapper extends BaseMapper<SpotifyToken> {

    /**
     * 根据用户ID查询Spotify Token
     *
     * @param accountId 用户ID
     * @return Spotify Token实体,不存在则返回null
     */
    @Select("SELECT * FROM spotify_token WHERE account_id = #{accountId} AND is_deleted = 0")
    SpotifyToken getByAccountId(Integer accountId);

    /**
     * 获取所有已绑定Spotify的用户ID列表
     * 用于定时任务批量刷新
     *
     * @return 用户ID列表
     */
    @Select("SELECT account_id FROM spotify_token WHERE is_deleted = 0")
    List<Integer> getAllConnectedAccountIds();
}
