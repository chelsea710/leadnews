package com.heima.article.service.impl;

import com.heima.article.service.ApArticleSearchService;
import com.heima.model.article.dtos.UserSearchDto;
import com.heima.model.article.pojos.ApAssociateWords;
import com.heima.model.article.pojos.ApHotWords;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mappers.app.ApAssociateWordsMapper;
import com.heima.model.mappers.app.ApBehaviorEntryMapper;
import com.heima.model.mappers.app.ApHotWordsMapper;
import com.heima.model.mappers.app.ApUserSearchMapper;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserSearch;
import com.heima.utils.threadlocal.AppThreadLocalUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@SuppressWarnings("all")
public class ApArticleSearchServiceImpl implements ApArticleSearchService {

    @Autowired
    private ApBehaviorEntryMapper apBehaviorEntryMapper;
    @Autowired
    private ApUserSearchMapper apUserSearchMapper;
    @Autowired
    private ApHotWordsMapper apHotWordsMapper;
    @Autowired
    private ApAssociateWordsMapper apAssociateWordsMapper;

    public ResponseResult getEntryId(UserSearchDto dto) {
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
        return ResponseResult.okResult(apBehaviorEntry.getId());
    }

    @Override
    public ResponseResult findUserSearch(UserSearchDto dto) {
        if (dto.getPageSize() > 50) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ResponseResult ret = getEntryId(dto);
        if (ret.getCode() != AppHttpCodeEnum.SUCCESS.getCode()) {
            return ret;
        }
        List<ApUserSearch> list = apUserSearchMapper.selectByEntryId((Integer) ret.getData(), dto.getPageSize());
        return ResponseResult.okResult(list);
    }


    @Override
    public ResponseResult delUserSearch(UserSearchDto dto) {
        if(dto.getHisList() ==null || dto.getHisList().size()<=0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        ResponseResult ret = getEntryId(dto);
        if(ret.getCode()!=AppHttpCodeEnum.SUCCESS.getCode()){
            return ret;
        }
        List<Integer> ids = dto.getHisList().stream().map(ApUserSearch::getId).collect(Collectors.toList());
        int rows = apUserSearchMapper.delUserSearch((Integer) ret.getData(),ids);
        return ResponseResult.okResult(rows);
    }

    @Override
    public ResponseResult clearUserSearch(UserSearchDto userSearchDto) {
        ResponseResult ret = getEntryId(userSearchDto);
        if(ret.getCode()!=AppHttpCodeEnum.SUCCESS.getCode()){
            return ret;
        }
        int rows = apUserSearchMapper.clearUserSearch((Integer) ret.getData());
        return ResponseResult.okResult(rows);
    }

    @Override
    public ResponseResult hotKeywords(String date) {
        if(StringUtils.isEmpty(date)){
            date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        }
        List<ApHotWords> list = apHotWordsMapper.queryByHotDate(date);
        return ResponseResult.okResult(list);
    }

    @Override
    public ResponseResult searchAssociate(UserSearchDto userSearchDto) {
        if(userSearchDto.getPageSize()>50 || userSearchDto.getPageSize() < 1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        List<ApAssociateWords> aw = apAssociateWordsMapper.selectByAssociateWords("%"+userSearchDto.getSearchWords()+"%", userSearchDto.getPageSize());
        return ResponseResult.okResult(aw);
    }
}
