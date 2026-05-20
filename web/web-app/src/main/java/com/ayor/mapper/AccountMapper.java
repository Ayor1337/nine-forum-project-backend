package com.ayor.mapper;

import org.apache.ibatis.annotations.Param;
import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import com.ayor.entity.vo.UserSearchVO;

public interface AccountMapper extends BaseMapper<Account> {


    @Select("select * from account where username = #{username}")
    Account getAccountByUsername(String username);

    @Select("select * from account where account_id = #{id}")
    Account getAccountById(Integer id);

    @Select("select avatar_url from account where account_id = #{accountId}")
    String getAvatarUrlById(Integer accountId);

    /**
     * 批量查询指定ID的用户记录。
     *
     * @param accountIds 用户ID列表
     * @return 用户实体列表
     */
    List<Account> getAccountsByIds(@Param("accountIds") List<Integer> accountIds);

    List<UserSearchVO> searchUsersForMention(@Param("keyword") String keyword, @Param("currentUserId") Integer currentUserId);
}
