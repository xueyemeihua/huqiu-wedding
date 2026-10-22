<template>
  <el-card header="库存统计">
    <el-row :gutter="16">
      <el-col :span="8"><div class="stat"><div class="num">{{ stock.totalStock || 0 }}</div><div>总库存</div></div></el-col>
      <el-col :span="8"><div class="stat"><div class="num">{{ stock.availableStock || 0 }}</div><div>可用</div></div></el-col>
      <el-col :span="8"><div class="stat warn"><div class="num">{{ stock.occupyStock || 0 }}</div><div>占用</div></div></el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { stockReport } from '../../api/report'
const stock = ref({})
onMounted(async () => { stock.value = await stockReport() })
</script>

<style scoped>
.stat { text-align:center; padding:20px; background:#f7f9fc; border-radius:6px; }
.stat .num { font-size:28px; font-weight:600; color:#409EFF; }
.stat.warn .num { color:#E6A23C; }
</style>