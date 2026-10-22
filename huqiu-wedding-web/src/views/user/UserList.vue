<template>
  <el-card>
    <el-form inline>
      <el-form-item label="姓名"><el-input v-model="query.keyword" clearable /></el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="onAdd">新增销售员</el-button>
    </el-form>
    <el-table :data="list" border style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">{{ row.role === 1 ? '管理员' : '销售员' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="onToggle(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px" background layout="total,prev,pager,next"
                   :total="total" :page-size="query.size" :current-page="query.current"
                   @current-change="p => { query.current = p; load() }" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑' : '新增'" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
      <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="登录账号"><el-input v-model="form.username" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" placeholder="不修改请留空" /></el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.role">
          <el-option label="商家管理员" :value="1" />
          <el-option label="销售员" :value="2" />
        </el-select>
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
import { pageUser, saveUser, toggleUser } from '../../api/user'
import { ElMessage } from 'element-plus'

const list = ref([]); const total = ref(0)
const query = reactive({ current: 1, size: 20, keyword: '', role: 2 })
const dialogVisible = ref(false)
const empty = { id:null, realName:'', phone:'', username:'', password:'', role:2, status:1 }
const form = reactive({ ...empty })

async function load() {
  const r = await pageUser(query); list.value = r.records; total.value = r.total
}
function onAdd() { Object.assign(form, empty); dialogVisible.value = true }
function onEdit(row) { Object.assign(form, row); form.password = ''; dialogVisible.value = true }
async function onSave() { await saveUser(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
async function onToggle(row) { await toggleUser(row.id); ElMessage.success('操作成功'); load() }
onMounted(load)
</script>