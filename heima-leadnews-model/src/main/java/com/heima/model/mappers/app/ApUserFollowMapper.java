package com.heima.model.mappers.app;

import com.heima.model.user.pojos.ApUserFollow;

public interface ApUserFollowMapper {
    ApUserFollow selectByFollowId(String burst, Long userId, Integer followId);
}
