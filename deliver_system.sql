/*
 Navicat Premium Data Transfer
 Source Server Type    : MySQL
 Source Schema         : deliver_system
 Target Server Type    : MySQL
 File Encoding         : 65001
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表 (sys_user)
-- 存储所有角色的基础信息
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名/账号',
                            `password` varchar(100) NOT NULL COMMENT '密码',
                            `role` varchar(20) NOT NULL COMMENT '角色: STUDENT-学生, MERCHANT-商家, RIDER-骑手, ADMIN-管理员',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 1.1. 商家详情表 (merchant_details)
-- 存储商家的特有信息，与 sys_user 表一对一关联
-- ----------------------------
DROP TABLE IF EXISTS `merchant_details`;
CREATE TABLE `merchant_details` (
                                    `user_id` bigint NOT NULL COMMENT '关联的用户ID (主键 & 外键)',
                                    `status` tinyint(1) DEFAULT '0' COMMENT '审核状态: 0-未提交, 1-待审核, 2-审核通过, 3-审核不通过',
                                    `license_url` varchar(255) DEFAULT NULL COMMENT '食品经营许可证URL',
                                    `id_card_front_url` varchar(255) DEFAULT NULL COMMENT '身份证正面URL',
                                    `id_card_back_url` varchar(255) DEFAULT NULL COMMENT '身份证背面URL',
                                    `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
                                    `category` varchar(50) DEFAULT NULL COMMENT '主营品类',
                                    `submit_time` datetime DEFAULT NULL COMMENT '最新审核提交时间',
                                    PRIMARY KEY (`user_id`),
                                    CONSTRAINT `fk_merchant_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家详情表';

-- ----------------------------
-- 1.2. 审核历史表 (review_history)
-- 记录商家的所有审核流程节点
-- ----------------------------
DROP TABLE IF EXISTS `review_history`;
CREATE TABLE `review_history` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `merchant_id` bigint NOT NULL COMMENT '关联的商家ID',
                                  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID (管理员或系统)',
                                  `action` varchar(50) NOT NULL COMMENT '操作: SUBMIT, APPROVE, REJECT',
                                  `comment` varchar(255) DEFAULT NULL COMMENT '备注 (如驳回原因)',
                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_history_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核历史表';

-- ----------------------------
-- 2. 分类表 (category)
-- 商家对自己店铺内的菜品进行分类
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `name` varchar(50) NOT NULL COMMENT '分类名称',
                            `sort` int DEFAULT '0' COMMENT '排序字段',
                            `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_category_merchant` (`merchant_id`),
                            CONSTRAINT `fk_category_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- ----------------------------
-- 3. 菜品表 (dish)
-- 具体的菜品信息
-- ----------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `name` varchar(100) NOT NULL COMMENT '菜品名称',
                        `category_id` bigint NOT NULL COMMENT '所属分类ID',
                        `price` decimal(10,2) NOT NULL COMMENT '单价',
                        `image` varchar(255) DEFAULT NULL COMMENT '图片URL',
                        `description` varchar(255) DEFAULT NULL COMMENT '描述',
                        `status` tinyint DEFAULT '1' COMMENT '售卖状态: 1-起售, 0-停售',
                        `stock` int DEFAULT '0' COMMENT '库存数量',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_dish_category` (`category_id`),
                        CONSTRAINT `fk_dish_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品管理表';

-- ----------------------------
-- 4. 订单主表 (orders)
-- 记录订单的基本信息和状态
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
                          `id` varchar(8) NOT NULL COMMENT '订单号 (8位16进制)',
                          `user_id` bigint NOT NULL COMMENT '下单用户ID',
                          `merchant_id` bigint NOT NULL COMMENT '商家ID',
                          `rider_id` bigint DEFAULT NULL COMMENT '骑手ID',
                          `status` tinyint DEFAULT '0' COMMENT '状态: 0-待支付, 1-待接单, 2-制作中, 3-待取货, 4-配送中, 5-已完成, 6-已取消',
                          `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                          `address` varchar(255) NOT NULL COMMENT '收货地址快照',
                          `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人',
                          `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                          `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
                          `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
                          `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
                          PRIMARY KEY (`id`),
                          KEY `idx_order_user` (`user_id`),
                          KEY `idx_order_merchant` (`merchant_id`),
                          KEY `idx_order_rider` (`rider_id`),
                          CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
                          CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- ----------------------------
-- 5. 订单明细表 (order_detail)
-- 记录订单中具体的菜品快照
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `order_id` varchar(8) NOT NULL COMMENT '关联订单ID',
                                `dish_id` bigint NOT NULL COMMENT '关联菜品ID(用于统计)',
                                `dish_name` varchar(100) NOT NULL COMMENT '菜品名称快照',
                                `dish_price` decimal(10,2) NOT NULL COMMENT '下单时单价快照',
                                `dish_image` varchar(255) DEFAULT NULL COMMENT '菜品图片快照',
                                `number` int NOT NULL COMMENT '购买数量',
                                PRIMARY KEY (`id`),
                                KEY `idx_detail_order` (`order_id`),
                                CONSTRAINT `fk_detail_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ----------------------------
-- 6. 配送信息表 (delivery_info)
-- 记录配送过程中的详细信息
-- ----------------------------
DROP TABLE IF EXISTS `delivery_info`;
CREATE TABLE `delivery_info` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `order_id` varchar(8) NOT NULL COMMENT '关联订单ID',
                                 `rider_id` bigint NOT NULL COMMENT '骑手ID',
                                 `status` tinyint DEFAULT '1' COMMENT '配送状态: 1-待取货, 2-配送中, 3-已送达',
                                 `current_lat` decimal(10,6) DEFAULT NULL COMMENT '当前纬度',
                                 `current_lng` decimal(10,6) DEFAULT NULL COMMENT '当前经度',
                                 `estimated_time` datetime DEFAULT NULL COMMENT '预计送达时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_delivery_order` (`order_id`),
                                 CONSTRAINT `fk_delivery_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `fk_delivery_rider` FOREIGN KEY (`rider_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送信息表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 7. 地址簿 (address_book)
-- 存储用户的收货地址信息
-- ----------------------------
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `user_id` bigint NOT NULL COMMENT '关联的用户ID',
                                `address` varchar(255) NOT NULL COMMENT '详细地址',
                                `contact_name` varchar(50) NOT NULL COMMENT '联系人',
                                `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
                                `is_default` tinyint(1) DEFAULT '0' COMMENT '是否为默认地址: 1-是, 0-否',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_address_user` (`user_id`),
                                CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址簿';
