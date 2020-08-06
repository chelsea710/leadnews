package com.heima.article.service;

import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;

public interface AppArticleInfoService {

    /**
     * 加载文章详情内容
     * @param articleId
     * @return
     */
    ResponseResult getArticleInfo(Integer articleId);
    /**
     * 加载文章详情的初始化配置信息 比如关注 ， 不喜欢 。 喜欢 订阅等
     */
    ResponseResult loadArticleBehavior(ArticleInfoDto articleInfoDto);
}
