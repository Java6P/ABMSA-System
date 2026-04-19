<template>
  <div class="batch-view">
    <el-row :gutter="24">
      <!-- Upload Panel -->
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Files /></el-icon>
              <span>批量情感分析</span>
              <el-tag type="info" size="small" style="margin-left:auto">支持 CSV / Excel</el-tag>
            </div>
          </template>

          <!-- Format Info -->
          <el-alert
            type="info"
            :closable="false"
            style="margin-bottom:16px"
          >
            <template #title>
              文件格式说明：CSV/Excel 文件须包含列 <strong>text</strong>（推文内容）和 <strong>target</strong>（分析目标），
              第一行为表头。
              <el-button link type="primary" size="small" @click="downloadTemplate">
                <el-icon><Download /></el-icon> 下载模板
              </el-button>
            </template>
          </el-alert>

          <!-- Upload Area -->
          <el-upload
            ref="uploadRef"
            class="batch-upload"
            drag
            action="#"
            :auto-upload="false"
            :multiple="false"
            accept=".csv,.xlsx,.xls"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="1"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">
              将 CSV/Excel 文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="upload-tip">文件大小不超过 10MB</div>
            </template>
          </el-upload>

          <!-- Progress -->
          <div v-if="uploading" style="margin-top:16px">
            <el-text>正在分析中...</el-text>
            <el-progress
              :percentage="uploadProgress"
              :striped="true"
              :striped-flow="true"
              style="margin-top:8px"
            />
          </div>

          <div style="margin-top:16px;display:flex;gap:12px">
            <el-button
              type="primary"
              :disabled="!selectedFile"
              :loading="uploading"
              @click="handleUpload"
            >
              <el-icon><MagicStick /></el-icon>
              开始批量分析
            </el-button>
            <el-button @click="resetUpload">重 置</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- Results Table -->
      <el-col :span="24" style="margin-top:20px">
        <el-card v-if="results.length > 0" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>分析结果</span>
              <el-tag type="success" size="small">共 {{ results.length }} 条</el-tag>
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

          <el-table :data="results" stripe border style="width:100%">
            <el-table-column type="index" label="#" width="55" />
            <el-table-column prop="target" label="分析目标" width="140" show-overflow-tooltip />
            <el-table-column prop="text" label="推文内容" show-overflow-tooltip />
            <el-table-column label="情感倾向" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="sentimentType(row.sentiment)" size="small">
                  {{ sentimentLabel(row.sentiment) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="置信度" width="140">
              <template #default="{ row }">
                <el-progress
                  :percentage="Math.round((row.confidence || 0) * 100)"
                  :color="sentimentColor(row.sentiment)"
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { batchAnalyze, exportCsv, exportExcel } from '@/api/analysis'
import { ElMessage } from 'element-plus'

const uploadRef = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const results = ref([])

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = null
}

function resetUpload() {
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  results.value = []
  uploadProgress.value = 0
}

async function handleUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  uploadProgress.value = 10

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  const timer = setInterval(() => {
    if (uploadProgress.value < 85) uploadProgress.value += 5
  }, 400)

  try {
    const data = await batchAnalyze(formData)
    results.value = Array.isArray(data) ? data : (data?.results || [])
    uploadProgress.value = 100
    ElMessage.success(`批量分析完成，共 ${results.value.length} 条结果`)
  } catch (e) {
    // mock data for demo
    results.value = [
      { target: 'Product A', text: 'Amazing quality!', sentiment: 'positive', confidence: 0.92 },
      { target: 'Service', text: 'Very poor service, very disappointed.', sentiment: 'negative', confidence: 0.88 },
      { target: 'Price', text: 'The price is reasonable for the features offered.', sentiment: 'neutral', confidence: 0.75 }
    ]
    uploadProgress.value = 100
    ElMessage.warning('后端未连接，显示模拟结果')
  } finally {
    clearInterval(timer)
    uploading.value = false
  }
}

function downloadTemplate() {
  const csv = 'text,target\n"I love this product!","Product"\n"Bad service","Service"'
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'batch_template.csv'
  a.click()
  URL.revokeObjectURL(url)
}

async function handleExportCsv() {
  try {
    const blob = await exportCsv()
    downloadBlob(blob, 'results.csv')
  } catch {
    exportResultsLocally('csv')
  }
}

async function handleExportExcel() {
  try {
    const blob = await exportExcel()
    downloadBlob(blob, 'results.xlsx')
  } catch {
    exportResultsLocally('csv')
    ElMessage.warning('Excel 导出失败，已改为 CSV 下载')
  }
}

function exportResultsLocally(type) {
  const header = 'target,text,sentiment,confidence'
  const rows = results.value.map(r =>
    `"${r.target}","${r.text}","${r.sentiment}",${r.confidence}`
  )
  const csv = [header, ...rows].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  downloadBlob(blob, `results.${type}`)
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

function sentimentType(s) {
  return s === 'positive' ? 'success' : s === 'negative' ? 'danger' : 'warning'
}

function sentimentLabel(s) {
  return s === 'positive' ? '积极 😊' : s === 'negative' ? '消极 😞' : '中性 😐'
}

function sentimentColor(s) {
  return s === 'positive' ? '#67C23A' : s === 'negative' ? '#F56C6C' : '#E6A23C'
}
</script>

<style scoped>
.batch-view { padding: 0; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; }
.batch-upload { width: 100%; }
.upload-icon { font-size: 48px; color: #c0c4cc; }
.upload-text { font-size: 14px; color: #606266; margin-top: 8px; }
.upload-text em { color: #409EFF; font-style: normal; }
.upload-tip { font-size: 12px; color: #909399; text-align: center; margin-top: 6px; }
</style>
