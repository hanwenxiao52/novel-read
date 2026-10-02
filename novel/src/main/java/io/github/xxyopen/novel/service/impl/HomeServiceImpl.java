package io.github.xxyopen.novel.service.impl;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dao.vo.HomeBookVO;
import io.github.xxyopen.novel.dao.vo.HomeFriendLinkVO;
import io.github.xxyopen.novel.manager.cache.FriendLinkCacheManager;
import io.github.xxyopen.novel.manager.cache.HomeBookCacheManager;
import io.github.xxyopen.novel.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/13
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeBookCacheManager homeBookCacheManager;

    private final FriendLinkCacheManager friendLinkCacheManager;

    @Override
    public RestResp<List<HomeBookVO>> listHomeBooks() {
        return RestResp.ok(homeBookCacheManager.listHomeBooks());
    }
}
