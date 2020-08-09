package com.heima.article.service.impl;

import com.heima.article.service.AppUserRelationService;
import com.heima.common.zookeeper.Sequence;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.behavior.dtos.FollowBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mappers.app.ApAuthorMapper;
import com.heima.model.mappers.app.ApUserFanMapper;
import com.heima.model.mappers.app.ApUserFollowMapper;
import com.heima.model.mappers.app.ApUserMapper;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserFan;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.user.service.AppFollowBehaviorService;
import com.heima.utils.common.BurstUtils;
import com.heima.utils.threadlocal.AppThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@SuppressWarnings("all")
public class AppUserRelationServiceImpl implements AppUserRelationService {


    @Autowired
    ApUserFollowMapper apUserFollowMapper;
    @Autowired
    ApUserFanMapper apUserFanMapper;
    @Autowired
    ApAuthorMapper apAuthorMapper;
    @Autowired
    ApUserMapper apUserMapper;
    @Autowired
    Sequence sequences;
    @Autowired
    AppFollowBehaviorService appFollowBehaviorService;


    @Override
    public ResponseResult follow(UserRelationDto dto) {

        //followid
        if(dto.getOperation()==null||dto.getOperation()<0||dto.getOperation()>1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"无效的operation参数");
        }
        Integer followId = dto.getUserId();
        if(dto.getUserId() == null && dto.getAuthorId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE,"followId或authorId不能为空");
        } else if (dto.getUserId() == null){
            ApAuthor aa = apAuthorMapper.selectById(dto.getAuthorId());
            if(aa!=null) {
                followId = aa.getUserId().intValue();
            }
        }
        //关注 或者 不关注
        if(followId==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"关注人不存在");
        }else {
            ApUser user = AppThreadLocalUtils.getUser();
            if(user!=null) {
                if(dto.getOperation()==0) {
                    //关注
                    return followByUserId(user, followId, dto.getArticleId());
                }else{
                    //不关注
                    return followCancelByUserId(user,followId);
                }
            }else{
                return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            }
        }
    }

    /**
     * 不关注行为
     * @param user
     * @param followId
     * @return
     */
    private ResponseResult followCancelByUserId(ApUser user, Integer followId) {
        ApUserFollow auf = apUserFollowMapper.selectByFollowId(BurstUtils.groudOne(user.getId()),user.getId(),followId);
        if(auf==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"未关注");
        }else{
            ApUserFan fan = apUserFanMapper.selectByFansId(BurstUtils.groudOne(followId), followId, user.getId());
            if (fan != null) {
                apUserFanMapper.deleteByFansId(BurstUtils.groudOne(followId), followId, user.getId());
            }
            return ResponseResult.okResult(apUserFollowMapper.deleteByFollowId(BurstUtils.groudOne(user.getId()),user.getId(),followId));
        }
    }

    /**
     * 关注操作
     * @param user fans
     * @param followId 文章作者
     * @param articleId 文章id
     * @return
     */
    private ResponseResult followByUserId(ApUser user, Integer followId, Integer articleId) {
        ApUser followUser = apUserMapper.selectById(followId);
        if(followUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"关注用户不存在");
        }
        ApUserFollow auf = apUserFollowMapper.selectByFollowId(BurstUtils.groudOne(user.getId()),user.getId(),followId);
        if(auf == null){
            ApUserFan fan = apUserFanMapper.selectByFansId(BurstUtils.groudOne(followId), followId, user.getId());
            if (fan == null) {
                fan = new ApUserFan();
                fan.setId(sequences.sequenceApUserFan());
                fan.setUserId(followId);
                fan.setFansId(user.getId());
                fan.setFansName(user.getName());
                fan.setLevel((short) 0);
                fan.setIsDisplay(true);
                fan.setIsShieldComment(false);
                fan.setIsShieldLetter(false);
                fan.setBurst(BurstUtils.encrypt(fan.getId(), fan.getUserId()));
                apUserFanMapper.insert(fan);
            }
            auf = new ApUserFollow();
            auf.setId(sequences.sequenceApUserFollow());
            auf.setUserId(user.getId());
            auf.setFollowId(followId);
            auf.setFollowName(followUser.getName());
            auf.setCreatedTime(new Date());
            auf.setLevel((short) 0);
            auf.setIsNotice(true);
            auf.setBurst(BurstUtils.encrypt(auf.getId(),auf.getUserId()));
            // 记录关注行为
            FollowBehaviorDto dto = new FollowBehaviorDto();
            dto.setFollowId(followId);
            dto.setArticleId(articleId);
            appFollowBehaviorService.saveFollowBehavior(dto);
            return ResponseResult.okResult(apUserFollowMapper.insert(auf));
        }else {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"已关注");
        }
    }
}
