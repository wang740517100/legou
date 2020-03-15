package cn.wangkf.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserInfo {

    private Long id;

    private String username;

}