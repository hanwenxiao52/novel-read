package io.github.xxyopen.novel.controller.front;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dao.vo.BookCategoryVO;
import io.github.xxyopen.novel.dao.vo.BookChapterAboutVO;
import io.github.xxyopen.novel.dao.vo.BookChapterVO;
import io.github.xxyopen.novel.dao.vo.BookCommentVO;
import io.github.xxyopen.novel.dao.vo.BookContentAboutVO;
import io.github.xxyopen.novel.dao.vo.BookInfoVO;
import io.github.xxyopen.novel.dao.vo.BookRankVO;
import io.github.xxyopen.novel.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 前台门户-小说模块 API 控制器
 *
 * @author xiongxiaoyang
 * @date 2022/5/14
 */
@Tag(name = "BookController", description = "前台门户-小说模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_BOOK_URL_PREFIX)
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 小说分类列表查询接口
     */
    @Operation(summary = "小说分类列表查询接口")
    @GetMapping("category/list")
    public RestResp<List<BookCategoryVO>> listCategory(
        @Parameter(description = "作品方向", required = true) Integer workDirection) {
        return bookService.listCategory(workDirection);
    }

    /**
     * 小说信息查询接口
     */
    @Operation(summary = "小说信息查询接口")
    @GetMapping("{id}")
    public RestResp<BookInfoVO> getBookById(
        @Parameter(description = "小说 ID") @PathVariable("id") Long bookId) {
        return bookService.getBookById(bookId);
    }

    /**
     * 增加小说点击量接口
     */
    @Operation(summary = "增加小说点击量接口")
    @PostMapping("visit")
    public RestResp<Void> addVisitCount(@Parameter(description = "小说ID") @RequestParam Long bookId) {
        return bookService.addVisitCount(bookId);
    }

    /**
     * 小说最新章节相关信息查询接口
     */
    @Operation(summary = "小说最新章节相关信息查询接口")
    @GetMapping("last_chapter/about")
    public RestResp<BookChapterAboutVO> getLastChapterAbout(
        @Parameter(description = "小说ID") @RequestParam Long bookId) {
        return bookService.getLastChapterAbout(bookId);
    }

    /**
     * 小说推荐列表查询接口
     */
    @Operation(summary = "小说推荐列表查询接口")
    @GetMapping("rec_list")
    public RestResp<List<BookInfoVO>> listRecBooks(
        @Parameter(description = "小说ID") Long bookId) throws NoSuchAlgorithmException {
        return bookService.listRecBooks(bookId);
    }

    /**
     * 小说章节列表查询接口
     */
    @Operation(summary = "小说章节列表查询接口")
    @GetMapping("chapter/list")
    public RestResp<List<BookChapterVO>> listChapters(
        @Parameter(description = "小说ID") Long bookId) {
        return bookService.listChapters(bookId);
    }

    /**
     * 小说内容相关信息查询接口
     */
    @Operation(summary = "小说内容相关信息查询接口")
    @GetMapping("content/{chapterId}")
    public RestResp<BookContentAboutVO> getBookContentAbout(
        @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.getBookContentAbout(chapterId);
    }

    /**
     * 获取上一章节ID接口
     */
    @Operation(summary = "获取上一章节ID接口")
    @GetMapping("pre_chapter_id/{chapterId}")
    public RestResp<Long> getPreChapterId(
        @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.getPreChapterId(chapterId);
    }

    /**
     * 获取下一章节ID接口
     */
    @Operation(summary = "获取下一章节ID接口")
    @GetMapping("next_chapter_id/{chapterId}")
    public RestResp<Long> getNextChapterId(
        @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.getNextChapterId(chapterId);
    }

    /**
     * 小说点击榜查询接口
     */
    @Operation(summary = "小说点击榜查询接口")
    @GetMapping("visit_rank")
    public RestResp<List<BookRankVO>> listVisitRankBooks() {
        return bookService.listVisitRankBooks();
    }

    /**
     * 热门小说排行榜查询接口
     */
    @Operation(summary = "热门小说排行榜查询接口")
    @GetMapping("hot_rank")
    public RestResp<List<BookRankVO>> listHotRankBooks(
        @Parameter(description = "查询数量") @RequestParam(value = "number", defaultValue = "10") Integer number) {
        return bookService.listHotRankBooks(number);
    }

    /**
     * 小说新书榜查询接口
     */
    @Operation(summary = "小说新书榜查询接口")
    @GetMapping("newest_rank")
    public RestResp<List<BookRankVO>> listNewestRankBooks() {
        return bookService.listNewestRankBooks();
    }

    /**
     * 小说更新榜查询接口
     */
    @Operation(summary = "小说更新榜查询接口")
    @GetMapping("update_rank")
    public RestResp<List<BookRankVO>> listUpdateRankBooks() {
        return bookService.listUpdateRankBooks();
    }

    /**
     * 小说最新评论查询接口
     */
    @Operation(summary = "小说最新评论查询接口")
    @GetMapping("comment/newest_list")
    public RestResp<BookCommentVO> listNewestComments(
        @Parameter(description = "小说ID") Long bookId) {
        return bookService.listNewestComments(bookId);
    }

}
