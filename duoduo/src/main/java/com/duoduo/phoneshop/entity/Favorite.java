package com.duoduo.phoneshop.entity;

import lombok.Data;
import java.util.Date;

/**
 * 收藏实体类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Data
public class Favorite {
    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 商品信息(关联查询用)
     */
    private Product product;
}