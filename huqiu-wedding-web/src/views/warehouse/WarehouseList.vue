<template>
  <el-card>
    <el-form inline>
      <el-form-item label="关键字"><el-input v-model="query.keyword" clearable /></el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="onAdd">新增仓库</el-button>
    </el-form>
    <el-table :data="list" border style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="warehouseName" label="仓库名称" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px" background layout="total,prev,pager,next"
                   :total="total" :page-size="query.size" :current-page="query.current"
                   @current-change="p => { query.current = p; load() }" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑仓库' : '新增仓库'" width="500px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="名称"><el-input v-model="form.warehouseName" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="onSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageWarehouse, saveWarehouse, deleteWarehouse } from '../../api/warehouse'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]); const total = ref(0)
const query = reactive({ current: 1, size: 20, keyword: '' })
const dialogVisible = ref(false)
const form = reactive({ id: null, warehouseName: '', remark: '', status: 1 })

async function load() {
  const r = await pageWarehouse(query)
  list.value = r.records; total.value = r.total
}
function onAdd() { Object.assign(form, { id:null, warehouseName:'', remark:'', status:1 }); dialogVisible.value = true }
function onEdit(row) { Object.assign(form, row); dialogVisible.value = true }
async function onSave() { await saveWarehouse(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
async function onDelete(row) {
  await ElMessageBox.confirm('确定删除该仓库？', '提示')
  await deleteWarehouse(row.id); ElMessage.success('删除成功'); load()
}
onMounted(load)
</script>