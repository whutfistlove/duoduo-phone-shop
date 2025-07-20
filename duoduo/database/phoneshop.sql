-- 创建数据库
CREATE DATABASE IF NOT EXISTS `phoneshop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `phoneshop`;
-- 充值记录表

CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `usern` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(255) NOT NULL COMMENT '密码',
                        `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                        `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                        `realN` varchar(50) DEFAULT NULL COMMENT '真实姓名',
                        `balance` decimal(10,2) DEFAULT '0.00' COMMENT '账户余额',
                        `role` varchar(20) DEFAULT 'USER' COMMENT '角色(USER/ADMIN)',
                        `status` int(1) DEFAULT '1' COMMENT '状态(0:禁用,1:正常)',
                        `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                        `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_usern` (`usern`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品分类表
CREATE TABLE `category` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `n` varchar(50) NOT NULL COMMENT '分类名称',
                            `description` varchar(200) DEFAULT NULL COMMENT '描述',
                            `sort` int(11) DEFAULT '0' COMMENT '排序',
                            `status` int(1) DEFAULT '1' COMMENT '状态(0:禁用,1:正常)',
                            `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE `product` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `categoryId` bigint(20) NOT NULL COMMENT '分类ID',
                           `n` varchar(100) NOT NULL COMMENT '商品名称',
                           `brand` varchar(50) DEFAULT NULL COMMENT '品牌',
                           `model` varchar(50) DEFAULT NULL COMMENT '型号',
                           `price` decimal(10,2) NOT NULL COMMENT '价格',
                           `stock` int(11) DEFAULT '0' COMMENT '库存',
                           `image` varchar(500) DEFAULT NULL COMMENT '商品图片',
                           `description` text COMMENT '商品描述',
                           `status` int(1) DEFAULT '1' COMMENT '状态(0:下架,1:上架)',
                           `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                           `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           KEY `idx_category` (`categoryId`),
                           CONSTRAINT `fk_product_category` FOREIGN KEY (`categoryId`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 购物车表
CREATE TABLE `cart` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `userId` bigint(20) NOT NULL COMMENT '用户ID',
                        `productId` bigint(20) NOT NULL COMMENT '商品ID',
                        `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
                        `selected` int(1) DEFAULT '1' COMMENT '是否选中(0:否,1:是)',
                        `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                        `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        KEY `idx_user` (`userId`),
                        KEY `idx_product` (`productId`),
                        CONSTRAINT `fk_cart_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`),
                        CONSTRAINT `fk_cart_product` FOREIGN KEY (`productId`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 收藏表
CREATE TABLE `favorite` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `userId` bigint(20) NOT NULL COMMENT '用户ID',
                            `productId` bigint(20) NOT NULL COMMENT '商品ID',
                            `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_user_product` (`userId`,`productId`),
                            KEY `idx_product` (`productId`),
                            CONSTRAINT `fk_favorite_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`),
                            CONSTRAINT `fk_favorite_product` FOREIGN KEY (`productId`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 收货地址表
CREATE TABLE `address` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `userId` bigint(20) NOT NULL COMMENT '用户ID',
                           `receiverN` varchar(50) NOT NULL COMMENT '收货人姓名',
                           `receiverPhone` varchar(20) NOT NULL COMMENT '收货人电话',
                           `province` varchar(50) DEFAULT NULL COMMENT '省',
                           `city` varchar(50) DEFAULT NULL COMMENT '市',
                           `district` varchar(50) DEFAULT NULL COMMENT '区',
                           `detailAddress` varchar(200) NOT NULL COMMENT '详细地址',
                           `isDefault` int(1) DEFAULT '0' COMMENT '是否默认(0:否,1:是)',
                           `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                           `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           KEY `idx_user` (`userId`),
                           CONSTRAINT `fk_address_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 订单表
CREATE TABLE `order` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT,
                         `orderNo` varchar(50) NOT NULL COMMENT '订单号',
                         `userId` bigint(20) NOT NULL COMMENT '用户ID',
                         `addressId` bigint(20) NOT NULL COMMENT '收货地址ID',
                         `totalAmount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                         `payAmount` decimal(10,2) NOT NULL COMMENT '实付金额',
                         `status` int(2) DEFAULT '0' COMMENT '订单状态(0:待支付,1:已支付,2:已发货,3:已完成,4:已取消)',
                         `payTime` datetime DEFAULT NULL COMMENT '支付时间',
                         `deliveryTime` datetime DEFAULT NULL COMMENT '发货时间',
                         `receiveTime` datetime DEFAULT NULL COMMENT '收货时间',
                         `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                         `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                         `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_order_no` (`orderNo`),
                         KEY `idx_user` (`userId`),
                         KEY `idx_address` (`addressId`),
                         CONSTRAINT `fk_order_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`),
                         CONSTRAINT `fk_order_address` FOREIGN KEY (`addressId`) REFERENCES `address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单详情表
CREATE TABLE `order_item` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `orderId` bigint(20) NOT NULL COMMENT '订单ID',
                              `productId` bigint(20) NOT NULL COMMENT '商品ID',
                              `productN` varchar(100) NOT NULL COMMENT '商品名称',
                              `productImage` varchar(500) DEFAULT NULL COMMENT '商品图片',
                              `price` decimal(10,2) NOT NULL COMMENT '商品单价',
                              `quantity` int(11) NOT NULL COMMENT '数量',
                              `totalAmount` decimal(10,2) NOT NULL COMMENT '小计金额',
                              `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_order` (`orderId`),
                              KEY `idx_product` (`productId`),
                              CONSTRAINT `fk_order_item_order` FOREIGN KEY (`orderId`) REFERENCES `order` (`id`),
                              CONSTRAINT `fk_order_item_product` FOREIGN KEY (`productId`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- 插入初始数据
-- 管理员账号
INSERT INTO `user` (`usern`, `password`, `email`, `role`) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb94VKbMeO8NQsJrMZHkx1uOdPkqSrGPqGWMvzGdK', 'admin@qq.com', 'ADMIN'); -- 密码: admin123

-- 商品分类
INSERT INTO `category` (`n`, `description`, `sort`) VALUES
                                                        ('华为', '华为手机系列', 1),
                                                        ('小米', '小米手机系列', 2),
                                                        ('苹果', '苹果手机系列', 3),
                                                        ('OPPO', 'OPPO手机系列', 4),
                                                        ('VIVO', 'VIVO手机系列', 5);

-- 示例商品数据
INSERT INTO `product` (`categoryId`, `n`, `brand`, `model`, `price`, `stock`, `image`, `description`) VALUES
                                                                                                          (1, '华为 Mate 50 Pro', '华为', 'Mate 50 Pro', 6799.00, 100, '/images/huawei-mate50pro.jpg', '华为Mate50 Pro 256GB 昆仑霞光 直屏旗舰 北斗卫星消息'),
                                                                                                          (1, '华为 P60 Pro', '华为', 'P60 Pro', 6988.00, 80, '/images/huawei-p60pro.jpg', '华为P60 Pro 256GB 洛可可白 超聚光夜视长焦'),
                                                                                                          (2, '小米 13 Pro', '小米', '13 Pro', 4999.00, 120, '/images/xiaomi-13pro.jpg', '小米13 Pro 12GB+256GB 陶瓷黑 第二代骁龙8处理器'),
                                                                                                          (2, '小米 13', '小米', '13', 3999.00, 150, '/images/xiaomi-13.jpg', '小米13 12GB+256GB 远山蓝 第二代骁龙8处理器'),
                                                                                                          (3, 'iPhone 14 Pro', '苹果', 'iPhone 14 Pro', 7999.00, 60, '/images/iphone-14pro.jpg', 'Apple iPhone 14 Pro 256GB 深空黑色'),
                                                                                                          (3, 'iPhone 14', '苹果', 'iPhone 14', 5999.00, 100, '/images/iphone-14.jpg', 'Apple iPhone 14 128GB 午夜色');