package com.region.lis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全配置 fail-close 行为校验：配置缺失时必须拒绝，而不是放行。
 */
class SecurityPropertiesTest {

    @Test
    @DisplayName("管理员名单为空时拒绝所有调用（fail-close）")
    void emptyAdminCodesDeniesAll() {
        SecurityProperties props = new SecurityProperties();
        props.setAdminOrgCodes(Collections.emptyList());

        assertFalse(props.isAdminOrg("ADMIN01"));
        assertFalse(props.isAdminOrg(null));
        assertFalse(props.isAdminOrg(""));
    }

    @Test
    @DisplayName("管理员名单命中时放行，忽略大小写与空格")
    void adminOrgCodeMatches() {
        SecurityProperties props = new SecurityProperties();
        props.setAdminOrgCodes(Arrays.asList("ADMIN01", " ADMIN02 "));

        assertTrue(props.isAdminOrg("ADMIN01"));
        assertTrue(props.isAdminOrg("admin02"));
        assertFalse(props.isAdminOrg("TOWN01"));
    }

    @Test
    @DisplayName("强制鉴权清单支持 Ant 风格路径匹配")
    void failClosePathMatches() {
        SecurityProperties props = new SecurityProperties();
        props.setFailClosePaths(Collections.singletonList("/auth/**"));

        assertTrue(props.isFailClosePath("/auth/reset-password"));
        assertFalse(props.isFailClosePath("/lis/upload"));

        props.setFailClosePaths(Collections.emptyList());
        assertFalse(props.isFailClosePath("/auth/reset-password"));
    }
}
