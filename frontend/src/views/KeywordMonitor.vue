<template>
  <div class="keyword-monitor">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ====== Keywords Tab ====== -->
      <el-tab-pane label="关键词管理" name="keywords">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openKeywordDialog()">
            <el-icon><Plus /></el-icon> 新增关键词
          </el-button>
          <el-button @click="fetchKeywords">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>

        <el-row :gutter="16" style="margin-top:16px">
          <el-col
            v-for="kw in keywords"
            :key="kw.id"
            :span="8"
            style="margin-bottom:16px"
          >
            <el-card class="kw-card" shadow="hover">
              <div class="kw-card-header">
                <span class="kw-name">{{ kw.keyword }}</span>
                <el-tag
                  :type="kw.enabled ? 'success' : 'info'"
                  size="small"
                >
                  {{ kw.enabled ? '启用' : '停用' }}
                </el-tag>
              </div>
              <div class="kw-desc">{{ kw.description || '暂无描述' }}</div>
              <div class="kw-meta">
                <span>最后爬取：{{ kw.lastCrawlTime ? formatTime(kw.lastCrawlTime) : '从未' }}</span>
              </div>
              <div class="kw-actions">
                <el-button size="small" plain @click="$router.push(`/keyword/${kw.id}`)">
                  <el-icon><View /></el-icon> 详情
                </el-button>
                <el-button size="small" plain type="primary" @click="handleTriggerCrawl(kw)">
                  <el-icon><Refresh /></el-icon> 爬取
                </el-button>
                <el-button size="small" plain @click="openKeywordDialog(kw)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  size="small"
                  plain
                  :type="kw.enabled ? 'warning' : 'success'"
                  @click="handleToggleKeyword(kw)"
                >
                  {{ kw.enabled ? '停用' : '启用' }}
                </el-button>
                <el-button size="small" plain type="danger" @click="handleDeleteKeyword(kw)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </el-card>
          </el-col>

          <el-col v-if="keywords.length === 0" :span="24">
            <el-empty description="暂无关键词，点击「新增关键词」开始" />
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ====== SubTargets Tab ====== -->
      <el-tab-pane label="子目标管理" name="subtargets">
        <div class="tab-toolbar">
          <el-select
            v-model="selectedKwId"
            placeholder="请选择关键词"
            style="width:220px"
            @change="fetchSubTargets"
          >
            <el-option
              v-for="kw in keywords"
              :key="kw.id"
              :label="kw.keyword"
              :value="kw.id"
            />
          </el-select>
          <el-button type="primary" :disabled="!selectedKwId" @click="openSubTargetDialog()">
            <el-icon><Plus /></el-icon> 新增子目标
          </el-button>
        </div>

        <el-table :data="subTargets" stripe border style="width:100%;margin-top:16px">
          <el-table-column prop="name" label="子目标名称" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="openSubTargetDialog(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.enabled ? 'warning' : 'success'"
                @click="handleToggleSubTarget(row)"
              >
                {{ row.enabled ? '停用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteSubTarget(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!selectedKwId" description="请先选择一个关键词" style="margin-top:40px" />
      </el-tab-pane>
    </el-tabs>

    <!-- Keyword Dialog -->
    <el-dialog
      v-model="kwDialogVisible"
      :title="editingKeyword ? '编辑关键词' : '新增关键词'"
      width="440px"
    >
      <el-form
        ref="kwFormRef"
        :model="kwForm"
        :rules="kwRules"
        label-width="100px"
      >
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="kwForm.keyword" placeholder="例：iPhone" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="kwForm.description" placeholder="可选描述" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="kwForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="kwDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveKeyword">保存</el-button>
      </template>
    </el-dialog>

    <!-- SubTarget Dialog -->
    <el-dialog
      v-model="stDialogVisible"
      :title="editingSubTarget ? '编辑子目标' : '新增子目标'"
      width="440px"
    >
      <el-form
        ref="stFormRef"
        :model="stForm"
        :rules="stRules"
        label-width="100px"
      >
        <el-form-item label="子目标名称" prop="name">
          <el-input v-model="stForm.name" placeholder="例：摄像头" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="stForm.description" placeholder="可选描述" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="stForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveSubTarget">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  listKeywords, createKeyword, updateKeyword,
  deleteKeyword, toggleKeyword
} from '@/api/keyword'
import {
  listSubTargets, createSubTarget, updateSubTarget,
  deleteSubTarget, toggleSubTarget
} from '@/api/subtarget'
import { triggerCrawl } from '@/api/monitor'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const activeTab = ref('keywords')
const keywords = ref([])
const subTargets = ref([])
const selectedKwId = ref(null)
const saving = ref(false)

// Keyword dialog
const kwDialogVisible = ref(false)
const kwFormRef = ref(null)
const editingKeyword = ref(null)
const kwForm = reactive({ keyword: '', description: '', enabled: true })
const kwRules = { keyword: [{ required: true, message: '请输入关键词', trigger: 'blur' }] }

// SubTarget dialog
const stDialogVisible = ref(false)
const stFormRef = ref(null)
const editingSubTarget = ref(null)
const stForm = reactive({ name: '', description: '', enabled: true })
const stRules = { name: [{ required: true, message: '请输入子目标名称', trigger: 'blur' }] }

onMounted(fetchKeywords)

async function fetchKeywords() {
  try {
    const data = await listKeywords()
    keywords.value = Array.isArray(data) ? data : (data?.list || [])
  } catch {
    keywords.value = [
      { id: 1, keyword: 'iPhone 15', description: '苹果手机讨论', enabled: true, lastCrawlTime: null },
      { id: 2, keyword: 'Tesla', description: '特斯拉相关', enabled: false, lastCrawlTime: null }
    ]
  }
}

async function fetchSubTargets() {
  if (!selectedKwId.value) return
  try {
    const data = await listSubTargets(selectedKwId.value)
    subTargets.value = Array.isArray(data) ? data : (data?.list || [])
  } catch {
    subTargets.value = []
  }
}

function openKeywordDialog(kw = null) {
  editingKeyword.value = kw
  if (kw) {
    Object.assign(kwForm, { keyword: kw.keyword, description: kw.description || '', enabled: kw.enabled })
  } else {
    Object.assign(kwForm, { keyword: '', description: '', enabled: true })
  }
  kwDialogVisible.value = true
}

async function saveKeyword() {
  await kwFormRef.value.validate()
  saving.value = true
  try {
    if (editingKeyword.value) {
      await updateKeyword(editingKeyword.value.id, kwForm)
      ElMessage.success('更新成功')
    } else {
      await createKeyword(kwForm)
      ElMessage.success('创建成功')
    }
    kwDialogVisible.value = false
    await fetchKeywords()
  } catch {
    await fetchKeywords()
  } finally {
    saving.value = false
  }
}

async function handleDeleteKeyword(kw) {
  await ElMessageBox.confirm(`确认删除关键词「${kw.keyword}」？`, '提示', { type: 'warning' })
  try {
    await deleteKeyword(kw.id)
    ElMessage.success('删除成功')
    await fetchKeywords()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleToggleKeyword(kw) {
  try {
    await toggleKeyword(kw.id, !kw.enabled)
    ElMessage.success(kw.enabled ? '已停用' : '已启用')
    await fetchKeywords()
  } catch {
    kw.enabled = !kw.enabled
    await fetchKeywords()
  }
}

async function handleTriggerCrawl(kw) {
  try {
    await triggerCrawl(kw.id)
    ElMessage.success(`已触发关键词「${kw.keyword}」爬取任务`)
  } catch {
    ElMessage.warning('爬取触发失败，请检查后端服务')
  }
}

function openSubTargetDialog(st = null) {
  editingSubTarget.value = st
  if (st) {
    Object.assign(stForm, { name: st.name, description: st.description || '', enabled: st.enabled })
  } else {
    Object.assign(stForm, { name: '', description: '', enabled: true })
  }
  stDialogVisible.value = true
}

async function saveSubTarget() {
  await stFormRef.value.validate()
  saving.value = true
  try {
    const payload = { ...stForm, keywordId: selectedKwId.value }
    if (editingSubTarget.value) {
      await updateSubTarget(editingSubTarget.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createSubTarget(payload)
      ElMessage.success('创建成功')
    }
    stDialogVisible.value = false
    await fetchSubTargets()
  } finally {
    saving.value = false
  }
}

async function handleDeleteSubTarget(st) {
  await ElMessageBox.confirm(`确认删除子目标「${st.name}」？`, '提示', { type: 'warning' })
  await deleteSubTarget(st.id)
  ElMessage.success('删除成功')
  await fetchSubTargets()
}

async function handleToggleSubTarget(st) {
  await toggleSubTarget(st.id, !st.enabled)
  ElMessage.success(st.enabled ? '已停用' : '已启用')
  await fetchSubTargets()
}

function formatTime(t) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.keyword-monitor { padding: 0; }
.tab-toolbar { display: flex; gap: 12px; align-items: center; }
.kw-card { height: 100%; }
.kw-card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.kw-name { font-size: 16px; font-weight: 600; color: #303133; }
.kw-desc { font-size: 13px; color: #909399; min-height: 20px; margin-bottom: 10px; }
.kw-meta { font-size: 12px; color: #c0c4cc; margin-bottom: 12px; }
.kw-actions { display: flex; flex-wrap: wrap; gap: 6px; }
</style>
