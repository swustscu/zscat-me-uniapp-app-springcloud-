package com.mallplus.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mallplus.common.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zscat
 * @since 2019-05-01
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("role_id")
    private Long roleId;

    @TableField("menu_id")
    private Long menuId;




    @Override
    public String toString() {
        return "SysRoleMenu{" +
        ", roleId=" + roleId +
        ", menuId=" + menuId +
        "}";
    }
}
