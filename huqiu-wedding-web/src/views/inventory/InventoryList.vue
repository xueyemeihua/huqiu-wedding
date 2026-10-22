<template>
  <el-card>
    <el-form inline>
      <el-form-item label="仓库">
        <el-select v-model="query.warehouseId" clearable placeholder="全部仓库" style="width: 150px">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.warehouseName" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品">
        <el-select v-model="query.goodsId" filterable clearable placeholder="全部商品" style="width: 200px">
          <el-option v-for="g in goodsList" :key="g.id" :label="`${g.skuCode} - ${g.goodsName}`" :value="g.id" />
        </el-select>
      </el-form-item>

      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="onAdjust">库存调整</el-button>

      <el-button type="warning" plain @click="onDownloadTemplate">下载导入模板</el-button>
      <el-upload
          action="#"
          :http-request="onUploadExcel"
          :show-file-list="false"
          accept=".xlsx,.xls"
          style="display: inline-block; margin-left: 10px;"
      >
        <el-button type="danger" plain>Excel 批量导入</el-button>
      </el-upload>
    </el-form>

    <el-table :data="list" border style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="80" />
      <!-- 修改这里：使用后端返回的名称字段 -->
      <el-table-column prop="warehouseName" label="仓库" min-width="120" />
      <el-table-column prop="goodsName" label="商品" min-width="180" />

      <el-table-column prop="totalStock" label="总库存" width="100" />
      <el-table-column prop="availableStock" label="可用库存" width="100" />
      <el-table-column prop="occupyStock" label="占用库存" width="100" />
      <el-table-column prop="version" label="版本" width="80" />
    </el-table>

    <el-pagination style="margin-top:12px" background layout="total,prev,pager,next"
                   :total="total" :page-size="query.size" :current-page="query.current"
                   @current-change="p => { query.current = p; load() }" />
  </el-card>

  <!-- 库存调整弹窗 -->
  <el-dialog v-model="dialogVisible" title="库存调整" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="仓库">
        <el-select v-model="form.warehouseId" placeholder="请选择仓库" style="width: 100%">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.warehouseName" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品">
        <el-select v-model="form.goodsId" filterable placeholder="请选择商品" style="width: 100%">
          <el-option v-for="g in goodsList" :key="g.id" :label="`${g.skuCode} - ${g.goodsName}`" :value="g.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="变动数量">
        <el-input-number v-model="form.num" :min="1" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.changeType">
          <el-option label="入库" :value="1" />
          <el-option label="出库" :value="2" />
          <el-option label="盘点调整" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="onSave">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageInventory, changeStock, downloadTemplate, importExcel } from '../../api/inventory'
import { pageWarehouse } from '../../api/warehouse'
import { pageGoods } from '../../api/goods'
import { ElMessage, ElLoading } from 'element-plus'

const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 20, warehouseId: null, goodsId: null })

// 下拉框数据源
const warehouses = ref([])
const goodsList = ref([])

const dialogVisible = ref(false)
const form = reactive({ warehouseId: null, goodsId: null, num: 1, changeType: 1, remark: '' })

async function load() {
  const r = await pageInventory(query)
  list.value = r.records
  total.value = r.total
}

function onAdjust() {
  Object.assign(form, { warehouseId: null, goodsId: null, num: 1, changeType: 1, remark: '' })
  dialogVisible.value = true
}

async function onSave() {
  if (!form.warehouseId || !form.goodsId) return ElMessage.warning('请选择仓库和商品')
  await changeStock(form);
  ElMessage.success('已提交');
  dialogVisible.value = false;
  load()
}

// 下载模板逻辑
async function onDownloadTemplate() {
  try {
    const res = await downloadTemplate()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '库存入库导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('模板下载失败')
  }
}

// 上传导入逻辑
async function onUploadExcel(options) {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)

  const loading = ElLoading.service({ text: '正在校验并导入数据...', background: 'rgba(0,0,0,0.5)' })
  try {
    await importExcel(formData)
    loading.close()
    ElMessage.success('导入成功')
    load()
  } catch (e) {
    loading.close()
  }
}

onMounted(async () => {
  load()
  // 获取基础数据供搜索和弹窗使用
  const [wRes, gRes] = await Promise.all([
    pageWarehouse({ current: 1, size: 1000 }),
    pageGoods({ current: 1, size: 1000 })
  ])
  warehouses.value = wRes.records
  goodsList.value = gRes.records
})
</script>