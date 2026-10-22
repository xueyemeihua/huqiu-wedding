<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="6"><el-card><div class="stat"><div class="num">{{ data.todayOrderCount || 0 }}</div><div>今日订单</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat"><div class="num">¥{{ data.todayAmount || 0 }}</div><div>今日成交</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat"><div class="num">¥{{ data.monthAmount || 0 }}</div><div>本月成交</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat"><div class="num">{{ (data.rentCount || 0) + '/' + (data.saleCount || 0) }}</div><div>租赁/售卖</div></div></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8"><el-card><div class="stat warn"><div class="num">{{ data.pendingPickup || 0 }}</div><div>待取货</div></div></el-card></el-col>
      <el-col :span="8"><el-card><div class="stat warn"><div class="num">{{ data.pendingReturn || 0 }}</div><div>待归还</div></div></el-card></el-col>
      <el-col :span="8"><el-card><div class="stat warn"><div class="num">{{ data.pendingTailor || 0 }}</div><div>待改衣</div></div></el-card></el-col>
    </el-row>
    <el-card style="margin-top:16px" header="销售员业绩排行（本月）">
      <el-table :data="data.salesRanking || []" border>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="userName" label="销售员" />
        <el-table-column prop="amount" label="成交金额" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { businessReport } from '../api/report'
const data = ref({})
const loading = ref(false)
onMounted(async () => {
  loading.value = true
  try { data.value = await businessReport() } finally { loading.value = false }
})
</script>

<style scoped>
.stat { text-align:center; padding:10px 0; }
.stat .num { font-size:26px; font-weight:600; color:#409EFF; }
.stat.warn .num { color:#E6A23C; }
</style>