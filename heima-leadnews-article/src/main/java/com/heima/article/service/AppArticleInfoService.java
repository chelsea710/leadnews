package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;

public interface AppArticleInfoService {

    /**
     * 加载文章详情内容
     * @param articleId
     * @return
     */
    ResponseResult getArticleInfo(Integer articleId);
}
