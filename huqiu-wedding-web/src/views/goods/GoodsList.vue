<template>
  <el-card>
    <el-form inline>
      <el-form-item label="款号"><el-input v-model="query.skuCode" clearable /></el-form-item>
      <el-form-item label="名称"><el-input v-model="query.goodsName" clearable /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="query.categoryId" clearable placeholder="全部分类">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="onAdd">新增商品</el-button>
    </el-form>
    <el-table :data="list" border style="margin-top:12px">
      <el-table-column prop="skuCode" label="款号" width="140" />
      <el-table-column prop="goodsName" label="名称" />
      <el-table-column prop="size" label="尺码" width="80" />
      <el-table-column prop="color" label="颜色" width="80" />
      <el-table-column prop="salePrice" label="售价" width="100" />
      <el-table-column prop="rentPrice" label="租赁价" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="onOffline(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px" background layout="total,prev,pager,next"
                   :total="total" :page-size="query.size" :current-page="query.current"
                   @current-change="p => { query.current = p; load() }" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="600px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="款号"><el-input v-model="form.skuCode" /></el-form-item>
      <el-form-item label="名称"><el-input v-model="form.goodsName" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="尺码"><el-input v-model="form.size" /></el-form-item>
      <el-form-item label="颜色"><el-input v-model="form.color" /></el-form-item>
      <el-form-item label="进价"><el-input-number v-model="form.purchasePrice" :precision="2" /></el-form-item>
      <el-form-item label="售价"><el-input-number v-model="form.salePrice" :precision="2" /></el-form-item>
      <el-form-item label="租赁价"><el-input-number v-model="form.rentPrice" :precision="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="onSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageGoods, saveGoods, offlineGoods, listCategories } from '../../api/goods'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]); const total = ref(0); const categories = ref([])
const query = reactive({ current: 1, size: 20, skuCode: '', goodsName: '', categoryId: null })
const dialogVisible = ref(false)
const empty = { id:null, skuCode:'', goodsName:'', categoryId:null, size:'', color:'',
  purchasePrice:0, salePrice:0, rentPrice:0, status:1 }
const form = reactive({ ...empty })

async function load() {
  const r = await pageGoods(query)
  list.value = r.records; total.value = r.total
}
function onAdd() { Object.assign(form, empty); dialogVisible.value = true }
function onEdit(row) { Object.assign(form, row); dialogVisible.value = true }
async function onSave() { await saveGoods(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
async function onOffline(row) {
  await ElMessageBox.confirm('确定下架？', '提示')
  await offlineGoods(row.id); ElMessage.success('已下架'); load()
}
onMounted(async () => { categories.value = await listCategories(); load() })
</script>