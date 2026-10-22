/*
 Navicat Premium Data Transfer

 Source Server         : 本机mysql
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : localhost:3306
 Source Schema         : huqiu-wedding

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 22/10/2026 21:53:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一款号',
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `size` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '尺码',
  `color` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
  `purchase_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '进价',
  `sale_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '售卖价',
  `rent_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '租赁价',
  `goods_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '1正常 0下架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `sku_code`(`sku_code` ASC) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_sku`(`sku_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '婚纱商品SKU' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, 1, '85001', '尚绅', 4, '55', '白色', 700.00, 1150.00, 300.00, NULL, 1, '2026-10-22 21:41:20', '2026-10-22 21:41:20');
INSERT INTO `goods` VALUES (2, 1, '85002', '蒂勃朗', 4, '55', '灰色', 1000.00, 2000.00, 600.00, NULL, 1, '2026-10-22 21:42:03', '2026-10-22 21:42:03');

-- ----------------------------
-- Table structure for goods_category
-- ----------------------------
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类：婚纱/秀禾/礼服/西装',
  `sort` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_category
-- ----------------------------
INSERT INTO `goods_category` VALUES (1, 1, '婚纱', 1, '2026-10-22 21:05:20');
INSERT INTO `goods_category` VALUES (2, 1, '秀禾', 2, '2026-10-22 21:05:20');
INSERT INTO `goods_category` VALUES (3, 1, '礼服', 3, '2026-10-22 21:05:20');
INSERT INTO `goods_category` VALUES (4, 1, '西装', 4, '2026-10-22 21:05:20');

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` int NOT NULL DEFAULT 0 COMMENT '可用库存',
  `occupy_stock` int NOT NULL DEFAULT 0 COMMENT '占用库存（订单+改衣锁定）',
  `version` int NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_goods`(`warehouse_id` ASC, `goods_id` ASC) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库库存' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory
-- ----------------------------
INSERT INTO `inventory` VALUES (3, 1, 1, 1, 200, 200, 0, 2, '2026-10-22 21:45:50', '2026-10-22 21:45:50');
INSERT INTO `inventory` VALUES (4, 1, 1, 2, 100, 99, 1, 3, '2026-10-22 21:45:50', '2026-10-22 21:45:59');

-- ----------------------------
-- Table structure for inventory_log
-- ----------------------------
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `change_type` tinyint NOT NULL COMMENT '1入库 2出库 3订单占用 4订单释放 5盘点调整',
  `change_num` int NOT NULL COMMENT '变动数量，正数增加，负数减少',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联订单号',
  `oper_user_id` bigint NOT NULL COMMENT '操作人',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存变动日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_log
-- ----------------------------
INSERT INTO `inventory_log` VALUES (1, 1, 1, 1, 1, 200, NULL, 1, 'Excel导入:存量', '2026-10-22 21:45:50');
INSERT INTO `inventory_log` VALUES (2, 1, 1, 2, 1, 100, NULL, 1, 'Excel导入:存量', '2026-10-22 21:45:50');
INSERT INTO `inventory_log` VALUES (3, 1, 1, 2, 3, -1, 'SO202610220001', 1, '开单锁定', '2026-10-22 21:45:59');

-- ----------------------------
-- Table structure for merchant
-- ----------------------------
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商户ID',
  `merchant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户名称',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门店地址',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1正常 0停用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchant
-- ----------------------------
INSERT INTO `merchant` VALUES (1, '虎丘婚纱城01店', '13800000000', '苏州虎丘婚纱城', 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20');

-- ----------------------------
-- Table structure for sale_order
-- ----------------------------
DROP TABLE IF EXISTS `sale_order`;
CREATE TABLE `sale_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '销售单号',
  `sale_user_id` bigint NOT NULL COMMENT '开单销售员ID',
  `customer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户姓名',
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户电话',
  `order_type` tinyint NOT NULL COMMENT '1售卖 2租赁',
  `total_amount` decimal(12, 2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL COMMENT '0草稿 1已开单 2已取货 3已归还 4已完成 9已取消',
  `pickup_time` datetime NULL DEFAULT NULL COMMENT '取货时间',
  `return_time` datetime NULL DEFAULT NULL COMMENT '归还时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_sale_user`(`sale_user_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '销售订单主单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sale_order
-- ----------------------------
INSERT INTO `sale_order` VALUES (1, 1, 'SO202610220001', 1, '李先生', '133', 1, 2000.00, 1, NULL, NULL, '定金500', '2026-10-22 21:43:24', '2026-10-22 21:43:24');

-- ----------------------------
-- Table structure for sale_order_item
-- ----------------------------
DROP TABLE IF EXISTS `sale_order_item`;
CREATE TABLE `sale_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `order_id` bigint NOT NULL COMMENT '销售订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '款号冗余，方便查询',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `num` int NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '销售订单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sale_order_item
-- ----------------------------
INSERT INTO `sale_order_item` VALUES (5, 1, 1, 'SO202610220001', 1, 2, '85002', 2000.00, 1, '2026-10-22 21:45:59');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NULL DEFAULT NULL,
  `oper_user_id` bigint NOT NULL,
  `oper_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作内容',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL COMMENT '所属商户ID',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `role` tinyint NOT NULL COMMENT '角色：1商家管理员 2销售员',
  `status` tinyint NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '老板', '13800000001', 1, 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20');
INSERT INTO `sys_user` VALUES (2, 1, 'sales01', 'e10adc3949ba59abbe56e057f20f883e', '销售小李', '13800000002', 2, 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20');

-- ----------------------------
-- Table structure for tailor_order
-- ----------------------------
DROP TABLE IF EXISTS `tailor_order`;
CREATE TABLE `tailor_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `tailor_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '改衣单号',
  `sale_order_id` bigint NOT NULL COMMENT '关联销售订单ID',
  `sale_order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联销售单号',
  `sale_user_id` bigint NOT NULL COMMENT '开单销售员',
  `customer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tailor_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '改衣费用',
  `plan_finish_time` datetime NULL DEFAULT NULL COMMENT '预计完工时间',
  `status` tinyint NOT NULL COMMENT '1待接单 2修改中 3已完工 4已交付 9已取消',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改项目描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `tailor_no`(`tailor_no` ASC) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_sale_order`(`sale_order_id` ASC) USING BTREE,
  INDEX `idx_tailor_no`(`tailor_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '改衣订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tailor_order
-- ----------------------------
INSERT INTO `tailor_order` VALUES (1, 1, 'TO202610220001', 1, 'SO202610220001', 1, '李先生', '133', 110.00, '2026-10-24 00:00:00', 9, '袖长+3，腰围-4', '2026-10-22 21:47:01', '2026-10-22 21:47:01');

-- ----------------------------
-- Table structure for warehouse
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `warehouse_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of warehouse
-- ----------------------------
INSERT INTO `warehouse` VALUES (1, 1, '主仓', '门店主仓库', 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20');
INSERT INTO `warehouse` VALUES (2, 1, '试衣间临时仓', '试衣间', 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20');

SET FOREIGN_KEY_CHECKS = 1;
