<template>
  <div style="padding:16px; max-width:480px; margin:0 auto">
    <van-cell-group inset title="快捷开单">
      <van-cell title="新建销售单" is-link @click="$router.push('/sale-order/create')" />
      <van-cell title="我的销售订单" is-link @click="$router.push('/sale-order')" />
      <van-cell title="我的改衣订单" is-link @click="$router.push('/tailor')" />
    </van-cell-group>
    <p style="color:#999;font-size:12px;margin-top:20px;text-align:center">
      微信小程序端可复用同一套接口，使用 Vant + 小程序适配层接入
    </p>
  </div>
</template>