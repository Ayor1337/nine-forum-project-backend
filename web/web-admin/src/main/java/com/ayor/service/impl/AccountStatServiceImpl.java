package com.ayor.service.impl;

import com.ayor.entity.pojo.AccountStat;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.service.AccountStatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountStatServiceImpl extends ServiceImpl<AccountStatMapper, AccountStat> implements AccountStatService {

}
