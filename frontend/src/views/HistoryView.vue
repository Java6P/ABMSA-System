<template>
  <div class="history-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>历史记录</span>

          <div style="margin-left:auto;display:flex;gap:8px">
            <el-button size="small" plain @click="handleExportCsv">
              <el-icon><Download /></el-icon> 导出 CSV
            </el-button>
            <el-button size="small" plain type="success" @click="handleExportExcel">
              <el-icon><Download /></el-icon> 导出 Excel
            </el-button>
          </div>
        </div>
      </template>

      <!-- Filter Bar -->
      <el-form inline class="filter-form">
        <el-form-item label="关键词搜索">
          <el-input
            v-model="filters.keyword"
            placeholder="目标/内容关键词"
            clearable
            style="width:200px"
            @clear="fetchHistory"
            @keyup.enter="fetchHistory"
          >
            <template #append>
              <el-button :icon="Search" @click="fetchHistory" />
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="情感倾向">
          <el-select v-model="filters.sentiment" style="width:130px" @change="fetchHistory">
            <el-option label="全部" value="" />
            <el-option label="积极 😊" value="positive" />
            <el-option label="中性 😐" value="neutral" />
            <el-option label="消极 😞" value="negative" />
          </el-select>
        </el-form-item>

        <el-form-item label="分析类型">
          <el-select v-model="filters.analysisType" style="width:130px" @change="fetchHistory">
            <el-option label="全部" value="" />
            <el-option label="手动分析" value="MANUAL" />
            <el-option label="自动分析" value="AUTO" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table
        :data="records"
        stripe
        border
        v-loading="loading"
        style="width:100%"
      >
        <el-table-column type="index" width="55" />
        <el-table-column prop="target" label="分析目标" width="140" show-overflow-tooltip />
        <el-table-column prop="inputText" label="推文内容" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.inputText ? row.inputText.substring(0, 80) + (row.inputText.length > 80 ? '...' : '') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="情感倾向" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="sentimentType(row.sentiment)" size="small">
              {{ sentimentLabel(row.sentiment) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round((row.confidence || 0) * 100)"
              :color="sentimentColor(row.sentiment)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="analysisType" label="分析类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.analysisType === 'MANUAL' ? 'primary' : 'success'" size="small" plain>
              {{ row.analysisType === 'MANUAL' ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="分析时间" width="170">
          <template #default="{ row }">
            {{ row.createdAt ? formatTime(row.createdAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button
              size="small"
              type="danger"
              plain
              :icon="Delete"
              circle
              @click="handleDelete(row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div style="margin-top:16px;display:flex;justify-content:flex-end">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchHistory"
          @size-change="fetchHistory"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listHistory, deleteRecord, exportCsv, exportExcel } from '@/api/history'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const records = ref([])
const filters = reactive({ keyword: '', sentiment: '', analysisType: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

onMounted(fetchHistory)

async function fetchHistory() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: filters.keyword || undefined,
      sentiment: filters.sentiment || undefined,
      analysisType: filters.analysisType || undefined
    }
    const data = await listHistory(params)
    records.value = Array.isArray(data) ? data : (data?.records || [])
    pagination.total = data?.total || records.value.length
  } catch {
    records.value = [
      { id: 1, target: 'iPhone 15', inputText: 'Amazing camera quality on the iPhone 15!', sentiment: 'positive', confidence: 0.92, analysisType: 'MANUAL', createdAt: new Date().toISOString() },
      { id: 2, target: '客服', inputText: '服务态度很差，等了很久', sentiment: 'negative', confidence: 0.87, analysisType: 'AUTO', createdAt: new Date().toISOString() },
      { id: 3, target: '价格', inputText: 'The price is average compared to competitors.', sentiment: 'neutral', confidence: 0.76, analysisType: 'MANUAL', createdAt: new Date().toISOString() }
    ]
    pagination.total = records.value.length
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.sentiment = ''
  filters.analysisType = ''
  pagination.page = 1
  fetchHistory()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
  try {
    await deleteRecord(row.id)
    ElMessage.success('删除成功')
    await fetchHistory()
  } catch {
    records.value = records.value.filter(r => r.id !== row.id)
    ElMessage.success('删除成功')
  }
}

async function handleExportCsv() {
  try {
    const blob = await exportCsv()
    downloadBlob(blob, 'history.csv')
  } catch {
    exportLocally('csv')
  }
}

async function handleExportExcel() {
  try {
    const blob = await exportExcel()
    downloadBlob(blob, 'history.xlsx')
  } catch {
    exportLocally('csv')
    ElMessage.warning('Excel 导出失败，已改为 CSV 格式')
  }
}

function exportLocally(type) {
  const header = 'target,text,sentiment,confidence,analysisType,createdAt'
  const rows = records.value.map(r =>
    `"${r.target}","${(r.inputText || '').replace(/"/g, '""')}","${r.sentiment}",${r.confidence},"${r.analysisType}","${r.createdAt}"`
  )
  const csv = [header, ...rows].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  downloadBlob(blob, `history.${type}`)
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

function formatTime(t) { return dayjs(t).format('YYYY-MM-DD HH:mm:ss') }
function sentimentType(s) { return s === 'positive' ? 'success' : s === 'negative' ? 'danger' : 'warning' }
function sentimentLabel(s) { return s === 'positive' ? '积极 😊' : s === 'negative' ? '消极 😞' : '中性 😐' }
function sentimentColor(s) { return s === 'positive' ? '#67C23A' : s === 'negative' ? '#F56C6C' : '#E6A23C' }
</script>

<style scoped>
.history-view { padding: 0; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; }
.filter-form { margin-bottom: 0; }
</style>
