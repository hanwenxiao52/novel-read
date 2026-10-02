package io.github.xxyopen.novel.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.xxyopen.novel.core.constant.CacheConsts;
import io.github.xxyopen.novel.dao.entity.BookInfo;
import io.github.xxyopen.novel.dao.mapper.BookInfoMapper;
import io.github.xxyopen.novel.dao.vo.BookRankVO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 热门小说排行榜 管理类（基于 Redis ZSet 实现）
 * <p>
 * ZSet 中：member=小说ID，score=点击量
 *
 * @author xiongxiaoyang
 * @date 2024/1/1
 */
@Component
@RequiredArgsConstructor
public class BookHotRankManager {

    private final StringRedisTemplate stringRedisTemplate;

    private final BookInfoMapper bookInfoMapper;

    /**
     * 小说点击量 +1（增量更新 ZSet 中对应小说的分数）
     */
    public void addVisitCount(Long bookId) {
        stringRedisTemplate.opsForZSet()
            .incrementScore(CacheConsts.BOOK_HOT_RANK_ZSET_KEY, String.valueOf(bookId), 1);
    }

    /**
     * 查询点击量排名前 number 的热门小说
     */
    public List<BookRankVO> listTopBooks(int number) {
        // 懒加载：ZSet 为空时，从数据库全量灌入一次
        Long size = stringRedisTemplate.opsForZSet().zCard(CacheConsts.BOOK_HOT_RANK_ZSET_KEY);
        if (size == null || size == 0) {
            initHotRank();
        }

        // 按点击量降序取前 number 名的小说ID（reverseRange 返回顺序即排名顺序）
        Set<String> bookIdSet = stringRedisTemplate.opsForZSet()
            .reverseRange(CacheConsts.BOOK_HOT_RANK_ZSET_KEY, 0, number - 1L);
        if (CollectionUtils.isEmpty(bookIdSet)) {
            return Collections.emptyList();
        }
        List<Long> bookIds = bookIdSet.stream().map(Long::valueOf).toList();

        // 批量查询小说详情
        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BookInfo::getId, bookIds);
        Map<Long, BookInfo> bookInfoMap = bookInfoMapper.selectList(queryWrapper).stream()
            .collect(Collectors.toMap(BookInfo::getId, Function.identity()));

        // 按排名顺序组装 VO
        return bookIds.stream().map(bookId -> {
            BookInfo bookInfo = bookInfoMap.get(bookId);
            if (Objects.isNull(bookInfo)) {
                return null;
            }
            BookRankVO bookRankVO = new BookRankVO();
            bookRankVO.setId(bookInfo.getId());
            bookRankVO.setCategoryId(bookInfo.getCategoryId());
            bookRankVO.setCategoryName(bookInfo.getCategoryName());
            bookRankVO.setBookName(bookInfo.getBookName());
            bookRankVO.setAuthorName(bookInfo.getAuthorName());
            bookRankVO.setPicUrl(bookInfo.getPicUrl());
            bookRankVO.setBookDesc(bookInfo.getBookDesc());
            bookRankVO.setLastChapterName(bookInfo.getLastChapterName());
            bookRankVO.setLastChapterUpdateTime(bookInfo.getLastChapterUpdateTime());
            bookRankVO.setWordCount(bookInfo.getWordCount());
            return bookRankVO;
        }).filter(Objects::nonNull).toList();
    }

    /**
     * 从 book_info 表全量同步点击量到 ZSet
     */
    private void initHotRank() {
        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BookInfo::getId, BookInfo::getVisitCount);
        List<BookInfo> bookInfos = bookInfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(bookInfos)) {
            return;
        }
        Set<ZSetOperations.TypedTuple<String>> tuples = bookInfos.stream()
            .map(v -> new DefaultTypedTuple<>(String.valueOf(v.getId()),
                Double.valueOf(v.getVisitCount())))
            .collect(Collectors.toSet());
        stringRedisTemplate.opsForZSet()
            .add(CacheConsts.BOOK_HOT_RANK_ZSET_KEY, tuples);
    }

}
