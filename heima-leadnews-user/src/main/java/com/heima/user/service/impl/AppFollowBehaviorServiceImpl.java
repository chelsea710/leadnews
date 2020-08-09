package com.heima.user.service.impl;

import com.heima.model.behavior.dtos.FollowBehaviorDto;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApFollowBehavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mappers.app.ApBehaviorEntryMapper;
import com.heima.model.mappers.app.ApFollowBehaviorMapper;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.AppFollowBehaviorService;
import com.heima.utils.threadlocal.AppThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@SuppressWarnings("all")
public class AppFollowBehaviorServiceImpl implements AppFollowBehaviorService {

    @Autowired
    private ApBehaviorEntryMapper apBehaviorEntryMapper;

    @Autowired
    private ApFollowBehaviorMapper apFollowBehaviorMapper;

    @Override
    public ResponseResult saveFollowBehavior(FollowBehaviorDto dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        // 用户和设备不能同时为空
        if(null == user && null == dto.getEquipmentId()){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        // 行为实体找以及注册了，逻辑上这里是必定有值得，除非参数错误
        Long userId = null;
        if(null != user){
            userId = user.getId();
        }
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryMapper.selectByUserIdOrEquipment(userId, dto.getEquipmentId());
        if(null == apBehaviorEntry){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }

        ApFollowBehavior apFollowBehavior = new ApFollowBehavior();
        apFollowBehavior.setArticleId(dto.getArticleId());
        apFollowBehavior.setCreatedTime(new Date());
        apFollowBehavior.setEntryId(apBehaviorEntry.getEntryId());
        apFollowBehavior.setFollowId(dto.getFollowId());
        int insert = apFollowBehaviorMapper.insert(apFollowBehavior);
        return ResponseResult.okResult(insert);
    }
}
