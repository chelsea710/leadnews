package com.heima.behavior.service.impl;

import com.heima.behavior.service.AppReadBehaviorService;
import com.heima.common.zookeeper.Sequence;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApReadBehavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mappers.app.ApBehaviorEntryMapper;
import com.heima.model.mappers.app.ApReadBehaviorMapper;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.BurstUtils;
import com.heima.utils.threadlocal.AppThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@SuppressWarnings("all")
public class AppReadBehaviorServiceImpl implements AppReadBehaviorService {

    @Autowired
    private ApReadBehaviorMapper apReadBehaviorMapper;
    @Autowired
    private ApBehaviorEntryMapper apBehaviorEntryMapper;
    @Autowired
    private Sequence sequences;

    @Override
    public ResponseResult saveReadBehavior(ReadBehaviorDto dto){
        ApUser user = AppThreadLocalUtils.getUser();
        // 用户和设备不能同时为空
        if(user==null&& dto.getEquipmentId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        Long userId = null;
        if(user!=null){
            userId = user.getId();
        }
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryMapper.selectByUserIdOrEquipment(userId, dto.getEquipmentId());
        // 行为实体找以及注册了，逻辑上这里是必定有值得，除非参数错误
        if(apBehaviorEntry==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApReadBehavior alb = apReadBehaviorMapper.selectByEntryId(BurstUtils.groudOne(apBehaviorEntry.getId()),apBehaviorEntry.getId(),dto.getArticleId());
        boolean isInsert = false;
        if(alb==null){
            alb = new ApReadBehavior();
            alb.setId(sequences.sequenceApReadBehavior());
            isInsert = true;
        }
        alb.setEntryId(apBehaviorEntry.getId());
        alb.setCount(dto.getCount());
        alb.setPercentage(dto.getPercentage());
        alb.setArticleId(dto.getArticleId());
        alb.setLoadDuration(dto.getLoadDuration());
        alb.setReadDuration(dto.getReadDuration());
        alb.setCreatedTime(new Date());
        alb.setUpdatedTime(new Date());
        alb.setBurst(BurstUtils.encrypt(alb.getId(),alb.getEntryId()));
        // 插入
        if(isInsert){
            return ResponseResult.okResult(apReadBehaviorMapper.insert(alb));
        }else {
            // 更新
            return ResponseResult.okResult(apReadBehaviorMapper.update(alb));
        }
    }
}
