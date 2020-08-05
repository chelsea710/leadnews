package com.heima.article.service.impl;

import com.heima.article.service.AppArticleInfoService;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.crawler.core.parse.ZipUtils;
import com.heima.model.mappers.app.ApArticleConfigMapper;
import com.heima.model.mappers.app.ApArticleContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("all")
public class AppArticleInfoServiceImpl implements AppArticleInfoService {

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;


    @Override
    public ResponseResult getArticleInfo(Integer articleId) {
        if(null == articleId || articleId < 1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        Map<String,Object> result = new HashMap<>();
        //1:查询config 判断文章是否被删除
        ApArticleConfig apArticleConfig = apArticleConfigMapper.selectByArticleId(articleId);
        if(apArticleConfig == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        } else if (!apArticleConfig.getIsDelete()){
            ApArticleContent apArticleContent = apArticleContentMapper.selectByArticleId(articleId);
            apArticleContent.setContent(ZipUtils.gunzip(apArticleContent.getContent()));
            result.put("content",apArticleContent);
        }
        result.put("config",apArticleConfig);

        return ResponseResult.okResult(result);
    }
}
