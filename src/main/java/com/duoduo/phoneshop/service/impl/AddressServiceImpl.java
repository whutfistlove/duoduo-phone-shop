package com.duoduo.phoneshop.service.impl;

import com.duoduo.phoneshop.entity.Address;
import com.duoduo.phoneshop.mapper.AddressMapper;
import com.duoduo.phoneshop.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地址服务实现类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> getUserAddresses(Long userId) {
        return addressMapper.selectByUser(userId);
    }

    @Override
    public Address getAddressById(Long id) {
        return addressMapper.selectById(id);
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        return addressMapper.selectDefaultByUser(userId);
    }

    @Override
    @Transactional
    public boolean addAddress(Address address) {
        try {
            // 如果设置为默认地址，需要先取消其他默认地址
            if (address.getIsDefault() == 1) {
                addressMapper.cancelDefaultByUser(address.getUserId());
            }

            return addressMapper.insert(address) > 0;
        } catch (Exception e) {
            log.error("添加地址失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateAddress(Address address) {
        try {
            // 如果设置为默认地址，需要先取消其他默认地址
            if (address.getIsDefault() == 1) {
                addressMapper.cancelDefaultByUser(address.getUserId());
            }

            return addressMapper.update(address) > 0;
        } catch (Exception e) {
            log.error("更新地址失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteAddress(Long id, Long userId) {
        try {
            Address address = addressMapper.selectById(id);
            if (address == null || !address.getUserId().equals(userId)) {
                return false;
            }

            return addressMapper.delete(id) > 0;
        } catch (Exception e) {
            log.error("删除地址失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Long id, Long userId) {
        try {
            Address address = addressMapper.selectById(id);
            if (address == null || !address.getUserId().equals(userId)) {
                return false;
            }

            // 取消其他默认地址
            addressMapper.cancelDefaultByUser(userId);

            // 设置为默认地址
            address.setIsDefault(1);
            return addressMapper.update(address) > 0;
        } catch (Exception e) {
            log.error("设置默认地址失败", e);
            return false;
        }
    }
}