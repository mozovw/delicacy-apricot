package com.delicacy.apricot.blog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.delicacy.apricot.blog.bean.Role;

import java.util.List;

/**
 * Created by sang on 2017/12/17.
 */
@Mapper
public interface RolesMapper {
    int addRoles(@Param("roles") String[] roles, @Param("uid") Long uid);

    List<Role> getRolesByUid(Long uid);
}
