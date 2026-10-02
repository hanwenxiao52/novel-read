package io.github.xxyopen.novel.service;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资源（图片/视频/文档）相关服务类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
public interface ResourceService {

    /**
     * 图片上传
     * @param file 需要上传的图片
     * @return 图片访问路径
     * */
    RestResp<String> uploadImage(MultipartFile file);
}
