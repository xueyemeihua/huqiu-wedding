<template>
  <el-card header="新建销售订单">
    <el-form :model="form" label-width="100px">
      <el-form-item label="客户姓名"><el-input v-model="form.customerName" /></el-form-item>
      <el-form-item label="客户电话"><el-input v-model="form.customerPhone" /></el-form-item>
      <el-form-item label="租售类型">
        <el-radio-group v-model="form.orderType">
          <el-radio :value="1">售卖</el-radio>
          <el-radio :value="2">租赁</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    </el-form>

    <h4>商品明细</h4>
    <el-table :data="form.items" border>
      <el-table-column label="仓库">
        <template #default="{ row }">
          <el-select v-model="row.warehouseId" placeholder="选择仓库">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.warehouseName" :value="w.id" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="商品">
        <template #default="{ row }">
          <el-select v-model="row.goodsId" filterable placeholder="选择商品">
            <el-option v-for="g in goodsList" :key="g.id"
                       :label="`${g.skuCode} - ${g.goodsName}`" :value="g.id" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="150">
        <template #default="{ row }"><el-input-number v-model="row.num" :min="1" /></template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ $index }">
          <el-button size="small" type="danger" @click="form.items.splice($index, 1)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button style="margin-top:12px" @click="form.items.push({ warehouseId: null, goodsId: null, num: 1 })">+ 添加商品</el-button>

    <div style="margin-top:20px">
      <el-button type="info" @click="onSubmit(true)">保存草稿</el-button>
      <el-button type="primary" @click="onSubmit(false)">提交开单</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pageWarehouse } from '../../api/warehouse'
import { pageGoods } from '../../api/goods'
import { createOrder } from '../../api/order'
import { ElMessage } from 'element-plus'

const router = useRouter()
const warehouses = ref([])
const goodsList = ref([])
const form = reactive({
  customerName: '', customerPhone: '', orderType: 2, remark: '',
  items: [{ warehouseId: null, goodsId: null, num: 1 }]
})

async function onSubmit(draft) {
  if (!form.customerName || !form.customerPhone) return ElMessage.warning('请填写客户信息')
  if (!form.items.length) return ElMessage.warning('请添加商品')
  await createOrder({ ...form, draft })
  ElMessage.success(draft ? '已保存草稿' : '开单成功，库存已锁定')
  router.push('/sale-order')
}

onMounted(async () => {
  const [w, g] = await Promise.all([
    pageWarehouse({ current: 1, size: 100 }),
    pageGoods({ current: 1, size: 200 })
  ])
  warehouses.value = w.records
  goodsList.value = g.records
})
</script>