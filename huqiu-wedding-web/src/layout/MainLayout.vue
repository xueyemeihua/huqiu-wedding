<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background:#304156; color:#fff">
      <div style="padding:18px; font-size:16px; font-weight:bold; color:#fff">虎丘婚纱城</div>
      <el-menu :default-active="route.path" router background-color="#304156"
               text-color="#bfcbd9" active-text-color="#409EFF">
        <template v-if="userStore.isAdmin()">
          <el-menu-item index="/dashboard">首页看板</el-menu-item>
          <el-menu-item index="/warehouse">仓库管理</el-menu-item>
          <el-menu-item index="/goods">商品SKU</el-menu-item>
          <el-menu-item index="/inventory">库存管理</el-menu-item>
          <el-menu-item index="/user">销售员管理</el-menu-item>
        </template>
        <el-menu-item index="/sale-order">销售订单</el-menu-item>
        <el-menu-item index="/sale-order/create">新建销售单</el-menu-item>
        <el-menu-item index="/tailor">改衣订单</el-menu-item>
        <template v-if="userStore.isAdmin()">
          <el-menu-item index="/report">经营报表</el-menu-item>
        </template>
        <el-menu-item index="/mobile">移动开单</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex; justify-content:space-between; align-items:center; background:#fff; border-bottom:1px solid #eee">
        <div>{{ route.meta.title }}</div>
        <div>
          <span style="margin-right:12px">{{ userStore.user?.realName }}（{{ userStore.user?.role === 1 ? '管理员' : '销售员' }}）</span>
          <el-button size="small" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main style="background:#f5f7fa">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
function onLogout() {
  userStore.logout()
  router.push('/login')
}
</script>