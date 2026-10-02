package io.github.xxyopen.novel.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.xxyopen.novel.core.constant.CacheConsts;
import io.github.xxyopen.novel.dao.entity.BookInfo;
import io.github.xxyopen.novel.dao.entity.HomeBook;
import io.github.xxyopen.novel.dao.mapper.BookInfoMapper;
import io.github.xxyopen.novel.dao.mapper.HomeBookMapper;
import io.github.xxyopen.novel.dao.vo.HomeBookVO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 首页推荐小说 缓存管理类
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@Component
@RequiredArgsConstructor
public class HomeBookCacheManager {

    private final HomeBookMapper homeBookMapper;

    private final BookInfoMapper bookInfoMapper;

    /**
     * 查询首页小说推荐，并放入缓存中
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
        value = CacheConsts.HOME_BOOK_CACHE_NAME)
    public List<HomeBookVO> listHomeBooks() {
        // 从首页小说推荐表中查询出需要推荐的小说
        LambdaQueryWrapper<HomeBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(HomeBook::getSort);
        List<HomeBook> homeBooks = homeBookMapper.selectList(queryWrapper);

        // 获取推荐小说ID列表
        if (!CollectionUtils.isEmpty(homeBooks)) {
            List<Long> bookIds = homeBooks.stream()
                .map(HomeBook::getBookId)
                .toList();

            // 根据小说ID列表查询相关的小说信息列表
            LambdaQueryWrapper<BookInfo> bookInfoQueryWrapper = new LambdaQueryWrapper<>();
            bookInfoQueryWrapper.in(BookInfo::getId, bookIds);
            List<BookInfo> bookInfos = bookInfoMapper.selectList(bookInfoQueryWrapper);

            // 组装 HomeBookRespDto 列表数据并返回
            if (!CollectionUtils.isEmpty(bookInfos)) {
                Map<Long, BookInfo> bookInfoMap = bookInfos.stream()
                    .collect(Collectors.toMap(BookInfo::getId, Function.identity()));
                return homeBooks.stream().map(v -> {
                    BookInfo bookInfo = bookInfoMap.get(v.getBookId());
                    HomeBookVO homeBookVO = new HomeBookVO();
                    homeBookVO.setType(v.getType());
                    homeBookVO.setBookId(v.getBookId());
                    homeBookVO.setBookName(bookInfo.getBookName());
                    homeBookVO.setPicUrl(bookInfo.getPicUrl());
                    homeBookVO.setAuthorName(bookInfo.getAuthorName());
                    homeBookVO.setBookDesc(bookInfo.getBookDesc());
                    return homeBookVO;
                }).toList();

            }

        }

        return Collections.emptyList();
    }

}
