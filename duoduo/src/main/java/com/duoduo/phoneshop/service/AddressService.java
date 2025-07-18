package com.duoduo.phoneshop.service;

import com.duoduo.phoneshop.entity.Address;
import java.util.List;

/**
 * 地址服务接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public interface AddressService {

    /**
     * 获取用户地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getUserAddresses(Long userId);

    /**
     * 根据ID查询地址
     * @param id 地址ID
     * @return 地址信息
     */
    Address getAddressById(Long id);

    /**
     * 获取用户默认地址
     * @param userId 用户ID
     * @return 默认地址
     */
    Address getDefaultAddress(Long userId);

    /**
     * 添加地址
     * @param address 地址信息
     * @return 添加结果
     */
    boolean addAddress(Address address);

    /**
     * 更新地址
     * @param address 地址信息
     * @return 更新结果
     */
    boolean updateAddress(Address address);

    /**
     * 删除地址
     * @param id 地址ID
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteAddress(Long id, Long userId);

    /**
     * 设置默认地址
     * @param id 地址ID
     * @param userId 用户ID
     * @return 设置结果
     */
    boolean setDefaultAddress(Long id, Long userId);
}