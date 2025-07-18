package com.duoduo.phoneshop.entity;

import lombok.Data;
import java.util.Date;

/**
 * 商品分类实体类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Data
public class Category {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String n;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 状态(0:禁用,1:正常)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;
}