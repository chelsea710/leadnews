package com.heima.article.apis;

import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ArticleInfoControllerApi {

    /**
     * 加載首頁详情
     * @param dto 封装参数对象
     * @return 文章详情
     */
    ResponseResult loadArticleInfo(ArticleInfoDto dto);
    /**
     * 加载文章详情的行为内容
     */
    ResponseResult loadArticleBehavior(ArticleInfoDto dto);
}
