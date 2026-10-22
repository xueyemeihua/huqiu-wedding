<template>
  <el-card>
    <el-form inline>
      <el-form-item label="客户">
        <el-input v-model="query.customerName" clearable/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部">
          <el-option label="草稿" :value="0"/>
          <el-option label="已开单" :value="1"/>
          <el-option label="已取货" :value="2"/>
          <el-option label="已归还" :value="3"/>
          <el-option label="已完成" :value="4"/>
          <el-option label="已取消" :value="9"/>
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="$router.push('/sale-order/create')">新建销售单</el-button>
    </el-form>

    <el-table :data="list" border style="margin-top:12px">
      <el-table-column prop="orderNo" label="订单号" width="180"/>
      <el-table-column prop="customerName" label="客户"/>
      <el-table-column prop="customerPhone" label="电话" width="140"/>
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ row.orderType === 1 ? '售卖' : '租赁' }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="100"/>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"/>
      <el-table-column label="操作" width="400">
        <template #default="{ row }">
          <el-button size="small" @click="onDetail(row)">{{ row.status === 0 ? '编辑/详情' : '详情' }}</el-button>
          <el-button size="small" type="primary" v-if="row.status === 1" @click="onChange(row, 2)">取货</el-button>
          <el-button size="small" type="primary" v-if="row.status === 2" @click="onChange(row, 3)">归还</el-button>
          <el-button size="small" type="success"
                     v-if="row.status === 3 || (row.orderType === 1 && (row.status === 1 || row.status === 2))"
                     @click="onChange(row, 4)">完成
          </el-button>

          <!-- 新增：改衣按钮 -->
          <el-button size="small" type="warning"
                     v-if="row.orderType === 1 && row.status !== 0 && row.status !== 9"
                     @click="onCreateTailor(row)">
            改衣
          </el-button>

          <el-button size="small" type="danger" v-if="row.status === 1 || row.status === 0" @click="onChange(row, 9)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px" background layout="total,prev,pager,next"
                   :total="total" :page-size="query.size" :current-page="query.current"
                   @current-change="p => { query.current = p; load() }"/>
  </el-card>

  <!-- 编辑/详情 抽屉 -->
  <el-drawer v-model="drawer" :title="detailForm.status === 0 ? '编辑草稿' : '订单详情'" size="600px">
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

      <h4 style="margin-top:20px; margin-bottom:10px;">商品明细</h4>
      <el-table :data="detailForm.items" border>
        <el-table-column label="仓库" min-width="120">
          <template #default="{ row }">
            <el-select v-if="detailForm.status === 0" v-model="row.warehouseId" placeholder="选择仓库" size="small">
              <el-option v-for="w in warehouses" :key="w.id" :label="w.warehouseName" :value="w.id"/>
            </el-select>
            <span v-else>{{ getWarehouseName(row.warehouseId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="150">
          <template #default="{ row }">
            <el-select v-if="detailForm.status === 0" v-model="row.goodsId" filterable placeholder="选择商品"
                       size="small" @change="(val) => onGoodsChange(val, row)">
              <el-option v-for="g in goodsList" :key="g.id" :label="`${g.skuCode} - ${g.goodsName}`" :value="g.id"/>
            </el-select>
            <span v-else>{{ getGoodsName(row.goodsId) }}</span>
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
        <el-table-column label="操作" width="80" v-if="detailForm.status === 0">
          <template #default="{ $index }">
            <el-button size="small" type="danger" @click="detailForm.items.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button v-if="detailForm.status === 0" style="margin-top:10px" size="small" @click="addItem">+ 添加商品
      </el-button>
    </el-form>

    <template #footer>
      <div style="flex: auto; display: flex; justify-content: flex-end; gap: 10px;">
        <el-button @click="drawer = false">关闭</el-button>
        <template v-if="detailForm.status === 0">
          <el-button type="danger" @click="onChange(detailForm, 9)">撤销（作废）</el-button>
          <el-button type="primary" @click="onSaveDraft">保存修改</el-button>
          <el-button type="success" @click="onSubmitDraft">提交开单</el-button>
        </template>
      </div>
    </template>
  </el-drawer>

  <!-- 新建改衣单弹窗 -->
  <el-dialog v-model="tailorDialogVisible" title="新建改衣单" width="500px">
    <el-form :model="tailorForm" label-width="110px">
      <el-form-item label="关联销售单">
        <el-input :value="currentSaleOrder?.orderNo" disabled />
      </el-form-item>
      <el-form-item label="客户">
        <el-input :value="currentSaleOrder?.customerName" disabled />
      </el-form-item>
      <el-form-item label="改衣费用">
        <el-input-number v-model="tailorForm.tailorCost" :precision="2" />
      </el-form-item>
      <el-form-item label="预计完工">
        <el-date-picker v-model="tailorForm.planFinishTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item label="修改项目">
        <el-input v-model="tailorForm.remark" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="tailorDialogVisible = false">取消</el-button>
      <el-button type="info" @click="onSaveTailor(true)">保存草稿</el-button>
      <el-button type="primary" @click="onSaveTailor(false)">直接提交</el-button>
    </template>
  </el-dialog>

</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {orderDetail, pageOrder, updateOrderDraft, updateOrderStatus} from '../../api/order'
import {pageWarehouse} from '../../api/warehouse'
import {pageGoods} from '../../api/goods'
import {ElMessage, ElMessageBox} from 'element-plus'
import {createTailor} from '../../api/tailor'

const list = ref([]);
const total = ref(0)
const query = reactive({current: 1, size: 20, status: null, customerName: ''})
const drawer = ref(false)
const detailForm = ref({items: []})
const warehouses = ref([])
const goodsList = ref([])

async function load() {
  const r = await pageOrder(query);
  list.value = r.records;
  total.value = r.total
}

async function onDetail(row) {
  const d = await orderDetail(row.id);
  detailForm.value = d;
  drawer.value = true;
}

async function onSaveDraft() {
  if (!detailForm.value.customerName || !detailForm.value.customerPhone) return ElMessage.warning('请填写客户信息');
  if (!detailForm.value.items.length) return ElMessage.warning('请添加商品');

  await updateOrderDraft(detailForm.value.id, detailForm.value);
  ElMessage.success('修改已保存');
  drawer.value = false;
  load();
}

async function onSubmitDraft() {
  if (!detailForm.value.customerName || !detailForm.value.customerPhone) return ElMessage.warning('请填写客户信息');
  if (!detailForm.value.items.length) return ElMessage.warning('请添加商品');

  await ElMessageBox.confirm('提交后将锁定相应库存，确定提交开单吗？', '提示');
  // 先保存修改
  await updateOrderDraft(detailForm.value.id, detailForm.value);
  // 再提交
  await updateOrderStatus({orderId: detailForm.value.id, status: 1});
  ElMessage.success('提交成功，库存已锁定');
  drawer.value = false;
  load();
}

async function onChange(row, status) {
  const msg = status === 9 ? '确定要撤销（取消）该订单吗？' : '确定执行该操作？'
  await ElMessageBox.confirm(msg, '提示')
  await updateOrderStatus({orderId: row.id, status});
  ElMessage.success('操作成功');
  drawer.value = false;
  load()
}

function addItem() {
  detailForm.value.items.push({warehouseId: null, goodsId: null, num: 1, price: 0})
}

function onGoodsChange(val, row) {
  const g = goodsList.value.find(item => item.id === val);
  if (g) {
    row.price = detailForm.value.orderType === 2 ? g.rentPrice : g.salePrice;
    row.skuCode = g.skuCode;
  }
}

function getWarehouseName(id) {
  return warehouses.value.find(w => w.id === id)?.warehouseName || id;
}

function getGoodsName(id) {
  const g = goodsList.value.find(item => item.id === id);
  return g ? `${g.skuCode} - ${g.goodsName}` : id;
}

function statusText(s) {
  return {0: '草稿', 1: '已开单', 2: '已取货', 3: '已归还', 4: '已完成', 9: '已取消'}[s] || '未知'
}

function statusType(s) {
  return {0: 'info', 1: 'warning', 2: 'primary', 3: '', 4: 'success', 9: 'danger'}[s] || ''
}

const tailorDialogVisible = ref(false)
const tailorForm = reactive({saleOrderId: null, tailorCost: 0, planFinishTime: null, remark: '', draft: false})
const currentSaleOrder = ref(null)

function onCreateTailor(row) {
  currentSaleOrder.value = row
  Object.assign(tailorForm, {saleOrderId: row.id, tailorCost: 0, planFinishTime: null, remark: '', draft: false})
  tailorDialogVisible.value = true
}

async function onSaveTailor(draft) {
  tailorForm.draft = draft;
  await createTailor(tailorForm)
  ElMessage.success(draft ? '已保存为草稿' : '改衣单创建成功，库存已锁定')
  tailorDialogVisible.value = false
}

onMounted(async () => {
  load()
  const [w, g] = await Promise.all([
    pageWarehouse({current: 1, size: 100}),
    pageGoods({current: 1, size: 500})
  ])
  warehouses.value = w.records
  goodsList.value = g.records
})
</script>