package com.heima.behavior.apis;

import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.ShowBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface BehaviorControllerApi {

    /**
     * 保存用户点击文章的行为
     * @param dto
     * @return
     */
    ResponseResult saveShowBehavior(ShowBehaviorDto dto);

    ResponseResult saveLikesBehavior(LikesBehaviorDto dto);

    ResponseResult saveUnLikesBehavior( UnLikesBehaviorDto dto) ;
    ResponseResult saveReadBehavior( ReadBehaviorDto dto);
}
