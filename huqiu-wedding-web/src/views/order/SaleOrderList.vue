<template>
  <el-card>
    <el-form inline>
      <el-form-item label="客户">
        <el-input v-model="query.customerName" clearable/>
      </el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="$router.push('/sale-order/create')">新建销售单</el-button>
    </el-form>

    <el-table :data="list" border style="margin-top:12px">
      <el-table-column prop="orderNo" label="订单号" width="180"/>
      <el-table-column prop="customerName" label="客户"/>
      <el-table-column prop="customerPhone" label="电话" width="130"/>
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ row.orderType === 1 ? '售卖' : '租赁' }}</template>
      </el-table-column>
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="420">
        <template #default="{ row }">
          <!-- 详情/编辑 -->
          <el-button size="small" @click="onDetail(row)">{{ row.status === 0 ? '编辑/详情' : '详情' }}</el-button>

          <!-- 仅租赁订单 + 已开单 -> 显示取货 -->
          <el-button size="small" type="primary"
                     v-if="row.orderType === 2 && row.status === 1"
                     @click="onChange(row, 2)">取货
          </el-button>

          <!-- 仅租赁订单 + 已取货 -> 显示归还 -->
          <el-button size="small" type="warning"
                     v-if="row.orderType === 2 && row.status === 2"
                     @click="onChange(row, 3)">归还
          </el-button>

          <!-- 售卖(已开单/已取货) 或 租赁(已归还) -> 显示完成 -->
          <el-button size="small" type="success"
                     v-if="(row.orderType === 1 && (row.status === 1 || row.status === 2)) || (row.orderType === 2 && row.status === 3)"
                     @click="onChange(row, 4)">完成
          </el-button>

          <!-- 仅售卖订单 + 已开单 -> 显示改衣 -->
          <!-- 仅售卖订单 + (已开单 或 已完成) -> 显示改衣 -->
          <el-button size="small" type="warning"
                     v-if="row.orderType === 1 && (row.status === 1 || row.status === 4)"
                     @click="openTailorDrawer(row)">改衣
          </el-button>

          <!-- 草稿或已开单 -> 显示取消 -->
          <el-button size="small" type="danger"
                     v-if="row.status === 0 || row.status === 1"
                     @click="onChange(row, 9)">取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px" background layout="total,prev,pager,next" :total="total"
                   :page-size="query.size" :current-page="query.current"
                   @current-change="p => { query.current = p; load() }"/>
  </el-card>

  <!-- ================= 订单详情 / 草稿编辑抽屉 ================= -->
  <el-drawer v-model="detailDrawerVisible" :title="detailForm.status === 0 ? '编辑草稿' : '订单详情'" size="650px">
    <el-form :model="detailForm" label-width="90px" :disabled="detailForm.status !== 0">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户姓名">
            <el-input v-model="detailForm.customerName"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户电话">
            <el-input v-model="detailForm.customerPhone"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="租售类型">
            <el-radio-group v-model="detailForm.orderType">
              <el-radio :value="1">售卖</el-radio>
              <el-radio :value="2">租赁</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总金额">
            <span style="font-size: 18px; color: #f56c6c; font-weight: bold;">¥{{ detailForm.totalAmount }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="detailForm.remark" type="textarea"/>
      </el-form-item>

      <h4 style="margin-top:20px; margin-bottom:10px; border-bottom: 1px solid #eee; padding-bottom: 10px;">
        商品明细</h4>
      <el-table :data="detailForm.items" border size="small">
        <el-table-column label="商品款号/名称" min-width="150">
          <template #default="{ row }">
            <!-- 草稿状态下允许重新选择商品（如果需要更复杂交互，此处可改为下拉框） -->
            <span>{{ row.skuCode }} - {{ getGoodsName(row.goodsId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" width="120">
          <template #default="{ row }">
            <el-input-number v-if="detailForm.status === 0" v-model="row.num" :min="1" size="small"
                             style="width: 90px"/>
            <span v-else>{{ row.num }}</span>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="100">
          <template #default="{ row }">¥{{ row.price * row.num }}</template>
        </el-table-column>
      </el-table>
    </el-form>

    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 10px;">
        <el-button @click="detailDrawerVisible = false">关闭</el-button>
        <template v-if="detailForm.status === 0">
          <el-button type="danger" @click="onChange(detailForm, 9)">撤销</el-button>
          <el-button type="primary" @click="onSaveDraft">保存修改</el-button>
          <el-button type="success" @click="onSubmitDraft">提交开单</el-button>
        </template>
      </div>
    </template>
  </el-drawer>

  <!-- ================= 改衣服务抽屉 ================= -->
  <el-drawer v-model="tailorDrawerVisible" title="改衣服务" size="600px">
    <div v-if="isCreatingTailor">
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 16px; font-weight: bold;">新建改衣单</span>
        <el-button v-if="relatedTailors.length > 0" size="small" @click="isCreatingTailor = false">返回列表</el-button>
      </div>
      <el-form :model="tailorForm" label-width="110px">
        <el-form-item label="关联销售单">
          <el-input :value="currentSaleOrder?.orderNo" disabled/>
        </el-form-item>
        <el-form-item label="客户">
          <el-input :value="currentSaleOrder?.customerName" disabled/>
        </el-form-item>
        <el-form-item label="改衣费用">
          <el-input-number v-model="tailorForm.tailorCost" :precision="2" :min="0"/>
        </el-form-item>
        <el-form-item label="预计完工">
          <el-date-picker v-model="tailorForm.planFinishTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss"
                          style="width: 100%"/>
        </el-form-item>
        <el-form-item label="修改项目">
          <el-input v-model="tailorForm.remark" type="textarea" rows="3" placeholder="请输入具体的修改要求"/>
        </el-form-item>
      </el-form>
      <div style="display:flex; gap:10px; justify-content:flex-end; margin-top:20px">
        <el-button type="info" @click="onSaveTailor(true)">保存草稿</el-button>
        <el-button type="primary" @click="onSaveTailor(false)">直接提交</el-button>
      </div>
    </div>

    <div v-else>
      <div style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: bold; font-size: 16px;">改衣进度</span>
        <!-- 增加限制：仅已开单状态允许在这里新增 -->
        <el-button v-if="currentSaleOrder?.status === 1" size="small" type="success" @click="enterCreateMode">+ 新增改衣单</el-button>
      </div>

      <el-timeline>
        <el-timeline-item v-for="t in relatedTailors" :key="t.id" :timestamp="t.createTime" placement="top"
                          :type="getTimelineType(t.status)">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between;">
                <span>单号: {{ t.tailorNo }}</span>
                <el-tag :type="getStatusType(t.status)">{{ getStatusText(t.status) }}</el-tag>
              </div>
            </template>

            <div v-if="editingTailorId !== t.id">
              <p style="margin: 5px 0"><strong>修改项目：</strong>{{ t.remark || '无' }}</p>
              <p style="margin: 5px 0"><strong>改衣费用：</strong><span style="color: #f56c6c">¥{{ t.tailorCost }}</span>
              </p>
              <p style="margin: 5px 0"><strong>预计完工：</strong>{{ t.planFinishTime || '未设置' }}</p>

              <div v-if="t.status === 0 || t.status === 1 || t.status === 2"
                   style="margin-top: 10px; text-align: right;">
                <el-button size="small" plain type="primary" @click="startEditTailor(t)">修改项目/金额</el-button>
              </div>
            </div>

            <div v-else>
              <el-form :model="editTailorForm" label-width="100px">
                <el-form-item label="改衣费用">
                  <el-input-number v-model="editTailorForm.tailorCost" :precision="2" :min="0"/>
                </el-form-item>
                <el-form-item label="预计完工">
                  <el-date-picker v-model="editTailorForm.planFinishTime" type="datetime"
                                  value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%"/>
                </el-form-item>
                <el-form-item label="修改项目">
                  <el-input v-model="editTailorForm.remark" type="textarea" rows="2"/>
                </el-form-item>
              </el-form>
              <div style="text-align: right; margin-top: 10px;">
                <el-button size="small" @click="editingTailorId = null">取消</el-button>
                <el-button size="small" type="primary" @click="onUpdateTailorInfo(t.id)">保存修改</el-button>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </el-drawer>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {pageOrder, updateOrderStatus, orderDetail, updateOrderDraft} from '../../api/order'
import {pageGoods} from '../../api/goods'
import {createTailor, listTailorBySaleId, updateTailorInfo} from '../../api/tailor'
import {ElMessage, ElMessageBox} from 'element-plus'

const list = ref([]);
const total = ref(0);
const query = reactive({current: 1, size: 20, customerName: ''})
const goodsList = ref([])

// ============ 详情抽屉逻辑 ============
const detailDrawerVisible = ref(false)
const detailForm = ref({items: []})

async function onDetail(row) {
  // 调用后端获取详情，包括主表和明细
  const d = await orderDetail(row.id)
  detailForm.value = d
  detailDrawerVisible.value = true
}

async function onSaveDraft() {
  await updateOrderDraft(detailForm.value.id, detailForm.value)
  ElMessage.success('修改已保存')
  detailDrawerVisible.value = false
  load()
}

async function onSubmitDraft() {
  await ElMessageBox.confirm('提交后将锁定商品库存，确定提交开单吗？', '提示')
  await updateOrderDraft(detailForm.value.id, detailForm.value)
  await updateOrderStatus({orderId: detailForm.value.id, status: 1})
  ElMessage.success('提交成功，库存已锁定')
  detailDrawerVisible.value = false
  load()
}

// ============ 改衣抽屉逻辑 ============
const tailorDrawerVisible = ref(false)
const currentSaleOrder = ref(null)
const relatedTailors = ref([])
const isCreatingTailor = ref(false)
const tailorForm = reactive({saleOrderId: null, tailorCost: 0, planFinishTime: null, remark: '', draft: false})
const editingTailorId = ref(null)
const editTailorForm = reactive({tailorCost: 0, planFinishTime: null, remark: ''})

async function openTailorDrawer(saleRow) {
  currentSaleOrder.value = saleRow
  tailorDrawerVisible.value = true
  editingTailorId.value = null
  relatedTailors.value = await listTailorBySaleId(saleRow.id)
  if (relatedTailors.value.length === 0) {
    enterCreateMode()
  } else {
    isCreatingTailor.value = false
  }
}

function enterCreateMode() {
  isCreatingTailor.value = true
  Object.assign(tailorForm, {
    saleOrderId: currentSaleOrder.value.id,
    tailorCost: 0,
    planFinishTime: null,
    remark: '',
    draft: false
  })
}

async function onSaveTailor(draft) {
  tailorForm.draft = draft
  await createTailor(tailorForm)
  ElMessage.success(draft ? '已保存为草稿' : '创建成功')
  relatedTailors.value = await listTailorBySaleId(currentSaleOrder.value.id)
  isCreatingTailor.value = false
}

function startEditTailor(t) {
  editingTailorId.value = t.id
  Object.assign(editTailorForm, {tailorCost: t.tailorCost, planFinishTime: t.planFinishTime, remark: t.remark})
}

async function onUpdateTailorInfo(tailorId) {
  await updateTailorInfo(tailorId, editTailorForm)
  ElMessage.success('修改成功')
  editingTailorId.value = null
  relatedTailors.value = await listTailorBySaleId(currentSaleOrder.value.id)
}

// ============ 通用逻辑 ============
async function load() {
  const r = await pageOrder(query);
  list.value = r.records;
  total.value = r.total
}

async function onChange(row, status) {
  const msg = status === 9 ? '确定要取消该订单吗？' : '确定执行该操作？'
  try {
    await ElMessageBox.confirm(msg, '提示')
    await updateOrderStatus({ orderId: row.id, status });
    ElMessage.success('操作成功');
    detailDrawerVisible.value = false;
    load()
  } catch (error) {
    // 如果用户点了"取消"按钮（ElMessageBox的取消，异常为 'cancel'），或者后端抛出了业务异常，都直接捕获，不执行后续逻辑
    // 后端的报错（如："该订单存在未完成的改衣单..."）已经由 request.js 全局拦截器通过 ElMessage.error 弹窗展示了
    console.log('操作中止:', error)
  }
}

function getGoodsName(id) {
  const g = goodsList.value.find(item => item.id === id);
  return g ? g.goodsName : '未知商品';
}

function statusText(s) {
  return {0: '草稿', 1: '已开单', 2: '已取货', 3: '已归还', 4: '已完成', 9: '已取消'}[s] || '未知'
}

function statusType(s) {
  return {0: 'info', 1: 'warning', 2: 'primary', 3: '', 4: 'success', 9: 'danger'}[s] || ''
}

function getStatusText(s) {
  return {0: '草稿', 1: '待接单', 2: '修改中', 3: '已完工', 4: '已交付', 9: '已取消'}[s] || '未知'
}

function getStatusType(s) {
  return {0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: '', 9: 'danger'}[s] || ''
}

function getTimelineType(s) {
  return s === 9 ? 'danger' : s === 4 ? 'success' : 'primary'
}

onMounted(async () => {
  load()
  // 获取商品列表用于详情页展示名称
  const gRes = await pageGoods({current: 1, size: 1000})
  goodsList.value = gRes.records
})
</script>