package com.heima.article.service.impl;

import com.google.common.collect.Maps;
import com.heima.article.service.AppArticleInfoService;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.article.pojos.ApCollection;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApLikesBehavior;
import com.heima.model.behavior.pojos.ApUnlikesBehavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.crawler.core.parse.ZipUtils;
import com.heima.model.mappers.app.*;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.utils.common.BurstUtils;
import com.heima.utils.threadlocal.AppThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("all")
public class AppArticleInfoServiceImpl implements AppArticleInfoService {


    @Autowired
    private ApBehaviorEntryMapper apBehaviorEntryMapper;

    @Autowired
    private ApUnlikesBehaviorMapper apUnlikesBehaviorMapper;

    @Autowired
    private ApLikesBehaviorMapper apLikesBehaviorMapper;

    @Autowired
    private ApAuthorMapper apAuthorMapper;

    @Autowired
    private ApUserFollowMapper apUserFollowMapper;

    @Autowired
    private ApCollectionMapper apCollectionMapper;

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


    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto articleInfoDto) {
        //定义返回值
        Map<String,Object> result = Maps.newHashMap();
        boolean isUnLike=false,isLike=false,isCollection=false,isFollow=false;
        ApUser user = AppThreadLocalUtils.getUser();
        // 用户和设备不能同时为空
        if(null == user && null == articleInfoDto.getEquipmentId()){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        // 行为实体找以及注册了，逻辑上这里是必定有值得，除非参数错误
        Long userId = null;
        if(null != user){
            userId = user.getId();
        }
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryMapper.selectByUserIdOrEquipment(userId, articleInfoDto.getEquipmentId());
        //构建brust
        String burstOne = BurstUtils.groudOne(apBehaviorEntry.getEntryId());
        Integer entryId = apBehaviorEntry.getEntryId();

        // 判断是否是已经不喜欢
        ApUnlikesBehavior apUnlikesBehavior = apUnlikesBehaviorMapper.selectLastUnLike(entryId, articleInfoDto.getArticleId());
        if(null != apUnlikesBehavior){
            isUnLike = true;
        }
        // 判断是否是已经喜欢
        ApLikesBehavior apLikesBehavior = apLikesBehaviorMapper.selectLastLike(burstOne, entryId, articleInfoDto.getArticleId(), ApLikesBehavior.Type.ARTICLE.getCode());
        if(null != apLikesBehavior && apLikesBehavior.getOperation() == ApLikesBehavior.Operation.LIKE.getCode()){
            isLike = true;
        }
        // 判断是否收藏
        ApCollection apCollection = apCollectionMapper.selectForEntryId(burstOne, entryId, articleInfoDto.getArticleId(), ApCollection.Type.ARTICLE.getCode());
        if(null != apBehaviorEntry){
            isCollection = true;
        }
        // 判断是否关注
        ApAuthor apAuthor = apAuthorMapper.selectById(articleInfoDto.getAuthorId());
        if(userId != null && apAuthor != null && apAuthor.getUserId() != null){
            ApUserFollow apUserFollow = apUserFollowMapper.selectByFollowId(BurstUtils.groudOne(userId), userId, apAuthor.getUserId().intValue());
            if(apUserFollow != null){
                isFollow = true;
            }
        }
        result.put("isfollow",isFollow);
        result.put("islike",isLike);
        result.put("isunlike",isUnLike);
        result.put("iscollection",isCollection);

        return ResponseResult.okResult(result);
    }
}
