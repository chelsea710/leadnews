package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;

public interface AppUserRelationService {

    public ResponseResult follow(UserRelationDto dto);
}
