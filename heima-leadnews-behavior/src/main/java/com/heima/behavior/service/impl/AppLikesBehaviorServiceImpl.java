package com.heima.behavior.service.impl;

import com.heima.behavior.service.AppLikesBehaviorService;
import com.heima.common.zookeeper.Sequence;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApLikesBehavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mappers.app.ApBehaviorEntryMapper;
import com.heima.model.mappers.app.ApLikesBehaviorMapper;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.BurstUtils;
import com.heima.utils.threadlocal.AppThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@SuppressWarnings("all")
public class AppLikesBehaviorServiceImpl implements AppLikesBehaviorService {

    @Autowired
    private ApLikesBehaviorMapper apLikesBehaviorMapper;
    @Autowired
    private ApBehaviorEntryMapper apBehaviorEntryMapper;
    @Autowired
    private Sequence sequences;

    @Override
    public ResponseResult saveLikesBehavior(LikesBehaviorDto dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        // 用户和设备不能同时为空
        if (user == null && dto.getEquipmentId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        Long userId = null;
        if (user != null) {
            userId = user.getId();
        }
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryMapper.selectByUserIdOrEquipment(userId, dto.getEquipmentId());
        // 行为实体找以及注册了，逻辑上这里是必定有值得，除非参数错误
        if (apBehaviorEntry == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApLikesBehavior alb = new ApLikesBehavior();
        alb.setId(sequences.sequenceApLikes());
        alb.setBehaviorEntryId(apBehaviorEntry.getId());
        alb.setCreatedTime(new Date());
        alb.setEntryId(dto.getEntryId());
        alb.setType(dto.getType());
        alb.setOperation(dto.getOperation());
        alb.setBurst(BurstUtils.encrypt(alb.getId(), alb.getBehaviorEntryId()));
        return ResponseResult.okResult(apLikesBehaviorMapper.insert(alb));

    }
}
