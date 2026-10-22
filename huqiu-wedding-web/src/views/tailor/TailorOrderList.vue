<template>
  <el-card>
    <el-form inline>
      <el-form-item label="销售单号"><el-input v-model="query.saleOrderNo" clearable /></el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
    </el-form>
    <el-table :data="list" border style="margin-top:12px">
      <el-table-column prop="tailorNo" label="改衣单号" width="180" />
      <el-table-column prop="saleOrderNo" label="销售单号" width="180" />
      <el-table-column prop="customerName" label="客户" />
      <el-table-column prop="tailorCost" label="改衣费" width="100" />
      <el-table-column prop="planFinishTime" label="预计完工" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360">
        <template #default="{ row }">
          <el-button size="small" @click="onDetail(row)">{{ row.status === 0 ? '编辑/详情' : '详情' }}</el-button>
          <el-button size="small" v-if="row.status === 1" @click="onChange(row, 2)">接单</el-button>
          <el-button size="small" v-if="row.status === 2" @click="onChange(row, 3)">完工</el-button>
          <el-button size="small" type="success" v-if="row.status === 3" @click="onChange(row, 4)">交付</el-button>
          <el-button size="small" type="danger" v-if="row.status !== 4 && row.status !== 9" @click="onChange(row, 9)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px" background layout="total,prev,pager,next"
                   :total="total" :page-size="query.size" :current-page="query.current"
                   @current-change="p => { query.current = p; load() }" />
  </el-card>

  <!-- 抽屉详情/编辑 -->
  <el-drawer v-model="drawer" :title="detailForm.status === 0 ? '编辑改衣草稿' : '改衣单详情'" size="500px">
    <el-form :model="detailForm" label-width="110px" :disabled="detailForm.status !== 0">
      <el-form-item label="关联销售单">
        <el-input :value="detailForm.saleOrderNo" disabled />
      </el-form-item>
      <el-form-item label="客户姓名">
        <el-input :value="detailForm.customerName" disabled />
      </el-form-item>
      <el-form-item label="客户电话">
        <el-input :value="detailForm.customerPhone" disabled />
      </el-form-item>
      <el-form-item label="改衣费用">
        <el-input-number v-model="detailForm.tailorCost" :precision="2" />
      </el-form-item>
      <el-form-item label="预计完工">
        <el-date-picker v-model="detailForm.planFinishTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item label="修改项目">
        <el-input v-model="detailForm.remark" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 10px;">
        <el-button @click="drawer = false">关闭</el-button>
        <template v-if="detailForm.status === 0">
          <el-button type="danger" @click="onCancelDraft">撤销</el-button>
          <el-button type="primary" @click="onSaveDraft">保存修改</el-button>
          <el-button type="success" @click="onSubmitDraft">提交</el-button>
        </template>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageTailor, updateTailorStatus, updateTailorDraft } from '../../api/tailor'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]); const total = ref(0)
const query = reactive({ current: 1, size: 20, saleOrderNo: '' })
const drawer = ref(false)
const detailForm = ref({})

async function load() {
  const r = await pageTailor(query); list.value = r.records; total.value = r.total
}

function onDetail(row) {
  detailForm.value = { ...row }
  drawer.value = true
}

async function onSaveDraft() {
  await updateTailorDraft(detailForm.value.id, detailForm.value)
  ElMessage.success('修改已保存')
  drawer.value = false
  load()
}

async function onSubmitDraft() {
  await ElMessageBox.confirm('提交后将锁定商品库存，确定提交吗？', '提示')
  await updateTailorDraft(detailForm.value.id, detailForm.value)
  await updateTailorStatus({ tailorId: detailForm.value.id, status: 1 })
  ElMessage.success('提交成功，库存已锁定')
  drawer.value = false
  load()
}

async function onCancelDraft() {
  await ElMessageBox.confirm('确定撤销该草稿吗？', '提示')
  await updateTailorStatus({ tailorId: detailForm.value.id, status: 9 })
  ElMessage.success('已撤销')
  drawer.value = false
  load()
}

async function onChange(row, status) {
  await ElMessageBox.confirm('确定执行该操作？', '提示')
  await updateTailorStatus({ tailorId: row.id, status }); ElMessage.success('操作成功'); load()
}

function statusText(s) {
  return { 0:'草稿', 1:'待接单', 2:'修改中', 3:'已完工', 4:'已交付', 9:'已取消' }[s] || '未知'
}
function statusType(s) {
  return { 0:'info', 1:'warning', 2:'primary', 3:'', 4:'success', 9:'danger' }[s] || ''
}

onMounted(load)
</script>