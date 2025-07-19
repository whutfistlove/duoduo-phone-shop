package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地址数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface AddressMapper {

    /**
     * 插入地址
     * @param address 地址信息
     * @return 影响行数
     */
    int insert(Address address);

    /**
     * 根据ID查询地址
     * @param id 地址ID
     * @return 地址信息
     */
    Address selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> selectByUser(@Param("userId") Long userId);

    /**
     * 查询用户默认地址
     * @param userId 用户ID
     * @return 默认地址
     */
    Address selectDefaultByUser(@Param("userId") Long userId);

    /**
     * 更新地址
     * @param address 地址信息
     * @return 影响行数
     */
    int update(Address address);

    /**
     * 删除地址
     * @param id 地址ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 取消用户所有默认地址
     * @param userId 用户ID
     * @return 影响行数
     */
    int cancelDefaultByUser(@Param("userId") Long userId);
}