-- `huqiu-wedding`.goods 定义

CREATE TABLE `goods`
(
    `id`             bigint                                                        NOT NULL AUTO_INCREMENT,
    `merchant_id`    bigint                                                        NOT NULL,
    `sku_code`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '唯一款号',
    `goods_name`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
    `category_id`    bigint                                                        NOT NULL COMMENT '分类ID',
    `size`           varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '尺码',
    `color`          varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '颜色',
    `purchase_price` decimal(10, 2)                                                DEFAULT NULL COMMENT '进价',
    `sale_price`     decimal(10, 2)                                                DEFAULT NULL COMMENT '售卖价',
    `rent_price`     decimal(10, 2)                                                DEFAULT NULL COMMENT '租赁价',
    `goods_img`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片URL',
    `status`         tinyint                                                       DEFAULT '1' COMMENT '1正常 0下架',
    `create_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `sku_code` (`sku_code`) USING BTREE,
    KEY              `idx_merchant` (`merchant_id`) USING BTREE,
    KEY              `idx_sku` (`sku_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='婚纱商品SKU';


-- `huqiu-wedding`.goods_category 定义

CREATE TABLE `goods_category`
(
    `id`            bigint                                                       NOT NULL AUTO_INCREMENT,
    `merchant_id`   bigint                                                       NOT NULL,
    `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类：婚纱/秀禾/礼服/西装',
    `sort`          int      DEFAULT '0',
    `create_time`   datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY             `idx_merchant` (`merchant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品分类';


-- `huqiu-wedding`.inventory 定义

CREATE TABLE `inventory`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `merchant_id`     bigint NOT NULL,
    `warehouse_id`    bigint NOT NULL COMMENT '仓库ID',
    `goods_id`        bigint NOT NULL COMMENT '商品ID',
    `total_stock`     int    NOT NULL DEFAULT '0' COMMENT '总库存',
    `available_stock` int    NOT NULL DEFAULT '0' COMMENT '可用库存',
    `occupy_stock`    int    NOT NULL DEFAULT '0' COMMENT '占用库存（订单+改衣锁定）',
    `version`         int    NOT NULL DEFAULT '1' COMMENT '乐观锁版本号',
    `create_time`     datetime        DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_warehouse_goods` (`warehouse_id`,`goods_id`) USING BTREE,
    KEY               `idx_merchant` (`merchant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='仓库库存';


-- `huqiu-wedding`.inventory_log 定义

CREATE TABLE `inventory_log`
(
    `id`           bigint  NOT NULL AUTO_INCREMENT,
    `merchant_id`  bigint  NOT NULL,
    `warehouse_id` bigint  NOT NULL,
    `goods_id`     bigint  NOT NULL,
    `change_type`  tinyint NOT NULL COMMENT '1入库 2出库 3订单占用 4订单释放 5盘点调整',
    `change_num`   int     NOT NULL COMMENT '变动数量，正数增加，负数减少',
    `order_no`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '关联订单号',
    `oper_user_id` bigint  NOT NULL COMMENT '操作人',
    `remark`       varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `create_time`  datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY            `idx_merchant` (`merchant_id`) USING BTREE,
    KEY            `idx_goods` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存变动日志';


-- `huqiu-wedding`.merchant 定义

CREATE TABLE `merchant`
(
    `id`            bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '商户ID',
    `merchant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户名称',
    `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '联系电话',
    `address`       varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店地址',
    `status`        tinyint                                                       DEFAULT '1' COMMENT '状态 1正常 0停用',
    `create_time`   datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商户信息表';


-- `huqiu-wedding`.sale_order 定义

CREATE TABLE `sale_order`
(
    `id`             bigint                                                       NOT NULL AUTO_INCREMENT,
    `merchant_id`    bigint                                                       NOT NULL,
    `order_no`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '销售单号',
    `sale_user_id`   bigint                                                       NOT NULL COMMENT '开单销售员ID',
    `customer_name`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户姓名',
    `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户电话',
    `order_type`     tinyint                                                      NOT NULL COMMENT '1售卖 2租赁',
    `total_amount`   decimal(12, 2)                                               NOT NULL COMMENT '订单总金额',
    `status`         tinyint                                                      NOT NULL COMMENT '0草稿 1已开单 2已取货 3已归还 4已完成 9已取消',
    `pickup_time`    datetime                                                      DEFAULT NULL COMMENT '取货时间',
    `return_time`    datetime                                                      DEFAULT NULL COMMENT '归还时间',
    `remark`         varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `create_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `order_no` (`order_no`) USING BTREE,
    KEY              `idx_merchant` (`merchant_id`) USING BTREE,
    KEY              `idx_sale_user` (`sale_user_id`) USING BTREE,
    KEY              `idx_order_no` (`order_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售订单主单';


-- `huqiu-wedding`.sale_order_item 定义

CREATE TABLE `sale_order_item`
(
    `id`           bigint                                                       NOT NULL AUTO_INCREMENT,
    `merchant_id`  bigint                                                       NOT NULL,
    `order_id`     bigint                                                       NOT NULL COMMENT '销售订单ID',
    `order_no`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `warehouse_id` bigint                                                       NOT NULL,
    `goods_id`     bigint                                                       NOT NULL,
    `sku_code`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '款号冗余，方便查询',
    `price`        decimal(10, 2)                                               NOT NULL COMMENT '单价',
    `num`          int                                                          NOT NULL DEFAULT '1' COMMENT '数量',
    `create_time`  datetime                                                              DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY            `idx_order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售订单明细';


-- `huqiu-wedding`.sys_oper_log 定义

CREATE TABLE `sys_oper_log`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `merchant_id`  bigint                                                        DEFAULT NULL,
    `oper_user_id` bigint NOT NULL,
    `oper_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作内容',
    `ip_address`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL,
    `create_time`  datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统操作日志';


-- `huqiu-wedding`.sys_user 定义

CREATE TABLE `sys_user`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `merchant_id` bigint                                                        NOT NULL COMMENT '所属商户ID',
    `username`    varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '登录账号',
    `password`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
    `real_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '姓名',
    `phone`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
    `role`        tinyint                                                       NOT NULL COMMENT '角色：1商家管理员 2销售员',
    `status`      tinyint                                                      DEFAULT '1' COMMENT '1启用 0禁用',
    `create_time` datetime                                                     DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime                                                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `username` (`username`) USING BTREE,
    KEY           `idx_merchant` (`merchant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统用户';


-- `huqiu-wedding`.tailor_order 定义

CREATE TABLE `tailor_order`
(
    `id`               bigint                                                       NOT NULL AUTO_INCREMENT,
    `merchant_id`      bigint                                                       NOT NULL,
    `tailor_no`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '改衣单号',
    `sale_order_id`    bigint                                                       NOT NULL COMMENT '关联销售订单ID',
    `sale_order_no`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联销售单号',
    `sale_user_id`     bigint                                                       NOT NULL COMMENT '开单销售员',
    `customer_name`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `customer_phone`   varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `tailor_cost`      decimal(10, 2)                                                DEFAULT '0.00' COMMENT '改衣费用',
    `plan_finish_time` datetime                                                      DEFAULT NULL COMMENT '预计完工时间',
    `status`           tinyint                                                      NOT NULL COMMENT '1待接单 2修改中 3已完工 4已交付 9已取消',
    `remark`           varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改项目描述',
    `create_time`      datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `tailor_no` (`tailor_no`) USING BTREE,
    KEY                `idx_merchant` (`merchant_id`) USING BTREE,
    KEY                `idx_sale_order` (`sale_order_id`) USING BTREE,
    KEY                `idx_tailor_no` (`tailor_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='改衣订单';


-- `huqiu-wedding`.warehouse 定义

CREATE TABLE `warehouse`
(
    `id`             bigint                                                       NOT NULL AUTO_INCREMENT,
    `merchant_id`    bigint                                                       NOT NULL COMMENT '商户ID',
    `warehouse_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
    `remark`         varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `status`         tinyint                                                       DEFAULT '1',
    `create_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY              `idx_merchant` (`merchant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='仓库';

INSERT INTO goods (id, merchant_id, sku_code, goods_name, category_id, `size`, color, purchase_price, sale_price,
                   rent_price, goods_img, status, create_time, update_time)
VALUES (1, 1, '85001', '尚绅', 4, '55', '白色', 700.00, 1150.00, 300.00, NULL, 1, '2026-10-22 21:41:20',
        '2026-10-22 21:41:20'),
       (2, 1, '85002', '蒂勃朗', 4, '55', '灰色', 1000.00, 2000.00, 600.00, NULL, 1, '2026-10-22 21:42:03',
        '2026-10-22 21:42:03');
INSERT INTO goods_category (id, merchant_id, category_name, sort, create_time)
VALUES (1, 1, '婚纱', 1, '2026-10-22 21:05:20'),
       (2, 1, '秀禾', 2, '2026-10-22 21:05:20'),
       (3, 1, '礼服', 3, '2026-10-22 21:05:20'),
       (4, 1, '西装', 4, '2026-10-22 21:05:20');
INSERT INTO inventory (id, merchant_id, warehouse_id, goods_id, total_stock, available_stock, occupy_stock, version,
                       create_time, update_time)
VALUES (3, 1, 1, 1, 199, 199, 0, 8, '2026-10-22 21:45:50', '2026-10-23 16:40:35'),
       (4, 1, 1, 2, 97, 97, 0, 8, '2026-10-22 21:45:50', '2026-10-23 16:44:09');
INSERT INTO inventory_log (id, merchant_id, warehouse_id, goods_id, change_type, change_num, order_no, oper_user_id,
                           remark, create_time)
VALUES (1, 1, 1, 1, 1, 200, NULL, 1, 'Excel导入:存量', '2026-10-22 21:45:50'),
       (2, 1, 1, 2, 1, 100, NULL, 1, 'Excel导入:存量', '2026-10-22 21:45:50'),
       (3, 1, 1, 2, 3, -1, 'SO202610220001', 1, '开单锁定', '2026-10-22 21:45:59'),
       (4, 1, 1, 2, 2, -1, 'SO202610220001', 1, '售卖完成出库', '2026-10-23 14:17:54'),
       (5, 1, 1, 1, 3, -1, 'SO202610230001', 1, '开单锁定', '2026-10-23 14:48:17'),
       (6, 1, 1, 1, 3, -1, 'SO202610230002', 1, '开单锁定', '2026-10-23 14:51:30'),
       (7, 1, 1, 1, 4, 1, 'SO202610230001', 1, '订单释放', '2026-10-23 15:59:21'),
       (11, 1, 1, 1, 2, -1, 'SO202610230002', 1, '售卖完成出库', '2026-10-23 16:10:27'),
       (12, 1, 1, 1, 3, -1, 'SO202610230003', 2, '开单锁定', '2026-10-23 16:40:18'),
       (13, 1, 1, 1, 4, 1, 'SO202610230003', 2, '订单释放', '2026-10-23 16:40:35');
INSERT INTO inventory_log (id, merchant_id, warehouse_id, goods_id, change_type, change_num, order_no, oper_user_id,
                           remark, create_time)
VALUES (14, 1, 1, 2, 3, -1, 'SO202610230004', 2, '开单锁定', '2026-10-23 16:40:55'),
       (15, 1, 1, 2, 2, -1, 'SO202610230004', 2, '售卖完成出库', '2026-10-23 16:42:31'),
       (16, 1, 1, 2, 3, -1, 'SO202610230005', 1, '开单锁定', '2026-10-23 16:44:05'),
       (17, 1, 1, 2, 2, -1, 'SO202610230005', 1, '售卖完成出库', '2026-10-23 16:44:09');
INSERT INTO merchant (id, merchant_name, contact_phone, address, status, create_time, update_time)
VALUES (1, '虎丘婚纱城01店', '13800000000', '苏州虎丘婚纱城', 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20');
INSERT INTO sale_order (id, merchant_id, order_no, sale_user_id, customer_name, customer_phone, order_type,
                        total_amount, status, pickup_time, return_time, remark, create_time, update_time)
VALUES (1, 1, 'SO202610220001', 1, '李先生', '133', 1, 2000.00, 4, '2026-10-23 14:16:45', NULL, '定金500',
        '2026-10-22 21:43:24', '2026-10-23 14:15:47'),
       (2, 1, 'SO202610230001', 1, '李先生', '133', 2, 300.00, 4, '2026-10-23 15:59:12', '2026-10-23 15:59:22',
        '定金100', '2026-10-23 14:47:15', '2026-10-23 14:47:15'),
       (3, 1, 'SO202610230002', 1, '李先生', '133', 1, 1150.00, 4, NULL, NULL, '定金300', '2026-10-23 14:51:18',
        '2026-10-23 14:51:18'),
       (4, 1, 'SO202610230003', 2, '汪先生', '133', 2, 300.00, 4, '2026-10-23 16:40:30', '2026-10-23 16:40:35',
        '定金100', '2026-10-23 16:40:18', '2026-10-23 16:40:18'),
       (5, 1, 'SO202610230004', 2, '汪先生', '133', 1, 2000.00, 4, NULL, NULL, '定金500', '2026-10-23 16:40:55',
        '2026-10-23 16:40:55'),
       (6, 1, 'SO202610230005', 1, '李先生', '133', 1, 2000.00, 4, NULL, NULL, '500', '2026-10-23 16:44:05',
        '2026-10-23 16:44:05');
INSERT INTO sale_order_item (id, merchant_id, order_id, order_no, warehouse_id, goods_id, sku_code, price, num,
                             create_time)
VALUES (5, 1, 1, 'SO202610220001', 1, 2, '85002', 2000.00, 1, '2026-10-22 21:45:59'),
       (7, 1, 2, 'SO202610230001', 1, 1, '85001', 300.00, 1, '2026-10-23 14:48:17'),
       (9, 1, 3, 'SO202610230002', 1, 1, '85001', 1150.00, 1, '2026-10-23 14:51:30'),
       (10, 1, 4, 'SO202610230003', 1, 1, '85001', 300.00, 1, '2026-10-23 16:40:18'),
       (11, 1, 5, 'SO202610230004', 1, 2, '85002', 2000.00, 1, '2026-10-23 16:40:55'),
       (12, 1, 6, 'SO202610230005', 1, 2, '85002', 2000.00, 1, '2026-10-23 16:44:05');
INSERT INTO sys_user (id, merchant_id, username, password, real_name, phone, `role`, status, create_time, update_time)
VALUES (1, 1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '老板', '13800000001', 1, 1, '2026-10-22 21:05:20',
        '2026-10-22 21:05:20'),
       (2, 1, 'sales01', 'e10adc3949ba59abbe56e057f20f883e', '销售小李', '13800000002', 2, 1, '2026-10-22 21:05:20',
        '2026-10-22 21:05:20');
INSERT INTO tailor_order (id, merchant_id, tailor_no, sale_order_id, sale_order_no, sale_user_id, customer_name,
                          customer_phone, tailor_cost, plan_finish_time, status, remark, create_time, update_time)
VALUES (1, 1, 'TO202610220001', 1, 'SO202610220001', 1, '李先生', '133', 110.00, '2026-10-24 00:00:00', 9,
        '袖长+3，腰围-4', '2026-10-22 21:47:01', '2026-10-22 21:47:01'),
       (2, 1, 'TO202610230001', 3, 'SO202610230002', 1, '李先生', '133', 60.00, '2026-10-24 20:00:00', 4, '袖长+5',
        '2026-10-23 14:56:06', '2026-10-23 16:09:01'),
       (3, 1, 'TO202610230002', 3, 'SO202610230002', 1, '李先生', '133', 20.00, '2026-10-24 00:00:00', 4, '腰围-3',
        '2026-10-23 15:21:14', '2026-10-23 15:21:14'),
       (4, 1, 'TO202610230003', 5, 'SO202610230004', 2, '汪先生', '133', 100.00, '2026-10-27 00:00:00', 4,
        '腰围-5，袖长+3，肩宽+2', '2026-10-23 16:41:34', '2026-10-23 16:41:34');
INSERT INTO warehouse (id, merchant_id, warehouse_name, remark, status, create_time, update_time)
VALUES (1, 1, '主仓', '门店主仓库', 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20'),
       (2, 1, '试衣间临时仓', '试衣间', 1, '2026-10-22 21:05:20', '2026-10-22 21:05:20');
