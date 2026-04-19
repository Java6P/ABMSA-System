<template>
  <div class="monitor-detail">
    <!-- Header -->
    <el-card shadow="never" style="margin-bottom:16px">
      <div class="detail-header">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <div class="keyword-info" v-if="keyword">
          <span class="kw-title">{{ keyword.keyword }}</span>
          <el-tag :type="keyword.enabled ? 'success' : 'info'" size="small">
            {{ keyword.enabled ? '启用中' : '已停用' }}
          </el-tag>
          <span class="kw-desc">{{ keyword.description }}</span>
        </div>
        <el-button type="primary" :loading="crawling" @click="handleTriggerCrawl" style="margin-left:auto">
          <el-icon><Refresh /></el-icon> 触发爬取
        </el-button>
      </div>
    </el-card>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tweets Tab -->
      <el-tab-pane label="推文列表" name="tweets">
        <el-table :data="tweets" stripe border v-loading="loadingTweets">
          <el-table-column type="index" width="55" />
          <el-table-column prop="content" label="推文内容" show-overflow-tooltip />
          <el-table-column prop="author" label="作者" width="130" />
          <el-table-column prop="publishTime" label="发布时间" width="170">
            <template #default="{ row }">
              {{ row.publishTime ? formatTime(row.publishTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="已分析" width="90" align="center">
            <template #default="{ row }">
              <el-icon v-if="row.isAnalyzed" color="#67C23A"><CircleCheckFilled /></el-icon>
              <el-icon v-else color="#C0C4CC"><CirclePlusFilled /></el-icon>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="tweetPage"
          v-model:page-size="tweetPageSize"
          :total="tweetTotal"
          layout="total, prev, pager, next"
          style="margin-top:12px;justify-content:flex-end"
          @current-change="fetchTweets"
        />
      </el-tab-pane>

      <!-- Results Tab -->
      <el-tab-pane label="分析结果" name="results">
        <el-table :data="analysisResults" stripe border v-loading="loadingResults">
          <el-table-column type="index" width="55" />
          <el-table-column prop="target" label="分析目标" width="140" />
          <el-table-column prop="text" label="推文内容" show-overflow-tooltip />
          <el-table-column label="情感倾向" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="sentimentType(row.sentiment)" size="small">
                {{ sentimentLabel(row.sentiment) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="置信度" width="130">
            <template #default="{ row }">
              <el-progress
                :percentage="Math.round((row.confidence || 0) * 100)"
                :color="sentimentColor(row.sentiment)"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="分析时间" width="170">
            <template #default="{ row }">
              {{ row.createdAt ? formatTime(row.createdAt) : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tasks Tab -->
      <el-tab-pane label="爬虫任务" name="tasks">
        <el-table :data="tasks" stripe border v-loading="loadingTasks">
          <el-table-column type="index" width="55" />
          <el-table-column prop="id" label="任务ID" width="80" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="taskStatusType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="triggerType" label="触发方式" width="110" align="center" />
          <el-table-column prop="tweetCount" label="推文数" width="90" align="center" />
          <el-table-column prop="startTime" label="开始时间" width="170">
            <template #default="{ row }">{{ row.startTime ? formatTime(row.startTime) : '-' }}</template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="170">
            <template #default="{ row }">{{ row.endTime ? formatTime(row.endTime) : '-' }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Logs Tab -->
      <el-tab-pane label="爬虫日志" name="logs">
        <div class="log-toolbar" style="margin-bottom:12px">
          <el-button size="small" @click="fetchLogs">
            <el-icon><Refresh /></el-icon> 刷新日志
          </el-button>
        </div>
        <div class="log-container">
          <div v-if="logs.length === 0" style="color:#909399;text-align:center;padding:40px">暂无日志</div>
          <div
            v-for="(log, i) in logs"
            :key="i"
            :class="['log-line', `log-${log.level || 'info'}`]"
          >
            <span class="log-time">{{ log.time || formatTime(log.createdAt) }}</span>
            <span class="log-level">[{{ (log.level || 'INFO').toUpperCase() }}]</span>
            <span class="log-msg">{{ log.message }}</span>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getKeyword } from '@/api/keyword'
import { listTasks, getTaskTweets, getTaskResults, triggerCrawl, getLogs } from '@/api/monitor'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const keywordId = Number(route.params.id)

const activeTab = ref('tweets')
const keyword = ref(null)
const crawling = ref(false)

const tweets = ref([])
const tweetPage = ref(1)
const tweetPageSize = ref(20)
const tweetTotal = ref(0)
const loadingTweets = ref(false)

const analysisResults = ref([])
const loadingResults = ref(false)

const tasks = ref([])
const loadingTasks = ref(false)

const logs = ref([])

onMounted(async () => {
  await fetchKeyword()
  await Promise.all([fetchTweets(), fetchTasks(), fetchLogs()])
})

async function fetchKeyword() {
  try {
    keyword.value = await getKeyword(keywordId)
  } catch {
    keyword.value = { id: keywordId, keyword: `关键词 #${keywordId}`, enabled: true, description: '' }
  }
}

async function fetchTweets() {
  loadingTweets.value = true
  try {
    const data = await getTaskTweets(keywordId, { page: tweetPage.value, size: tweetPageSize.value })
    tweets.value = data?.list || data || []
    tweetTotal.value = data?.total || tweets.value.length
  } catch {
    tweets.value = []
  } finally {
    loadingTweets.value = false
  }
}

async function fetchTasks() {
  loadingTasks.value = true
  try {
    const data = await listTasks(keywordId)
    tasks.value = Array.isArray(data) ? data : (data?.list || [])
    if (tasks.value.length > 0) {
      await fetchResults(tasks.value[0].id)
    }
  } catch {
    tasks.value = []
  } finally {
    loadingTasks.value = false
  }
}

async function fetchResults(taskId) {
  loadingResults.value = true
  try {
    const data = await getTaskResults(taskId)
    analysisResults.value = Array.isArray(data) ? data : (data?.list || [])
  } catch {
    analysisResults.value = []
  } finally {
    loadingResults.value = false
  }
}

async function fetchLogs() {
  try {
    const data = await getLogs({ keywordId })
    logs.value = Array.isArray(data) ? data : (data?.list || [])
  } catch {
    logs.value = [
      { time: dayjs().format('HH:mm:ss'), level: 'info', message: '系统就绪，等待爬取任务' }
    ]
  }
}

async function handleTriggerCrawl() {
  crawling.value = true
  try {
    await triggerCrawl(keywordId)
    ElMessage.success('爬取任务已触发')
    setTimeout(fetchTasks, 2000)
  } catch {
    ElMessage.warning('触发失败，请检查后端服务')
  } finally {
    crawling.value = false
  }
}

function formatTime(t) { return dayjs(t).format('YYYY-MM-DD HH:mm:ss') }
function sentimentType(s) { return s === 'positive' ? 'success' : s === 'negative' ? 'danger' : 'warning' }
function sentimentLabel(s) { return s === 'positive' ? '积极' : s === 'negative' ? '消极' : '中性' }
function sentimentColor(s) { return s === 'positive' ? '#67C23A' : s === 'negative' ? '#F56C6C' : '#E6A23C' }
function taskStatusType(s) {
  return s === 'COMPLETED' ? 'success' : s === 'RUNNING' ? 'primary' : s === 'FAILED' ? 'danger' : 'info'
}
</script>

<style scoped>
.monitor-detail { padding: 0; }
.detail-header { display: flex; align-items: center; gap: 12px; }
.kw-title { font-size: 18px; font-weight: 600; }
.kw-desc { font-size: 13px; color: #909399; }
.log-container {
  background: #1e1e1e;
  border-radius: 6px;
  padding: 12px 16px;
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  font-family: monospace;
  font-size: 13px;
}
.log-line { padding: 2px 0; display: flex; gap: 10px; }
.log-time { color: #6a9955; min-width: 80px; }
.log-level { min-width: 60px; }
.log-info .log-level { color: #4fc1ff; }
.log-warn .log-level { color: #e6a23c; }
.log-error .log-level { color: #f56c6c; }
.log-msg { color: #d4d4d4; flex: 1; word-break: break-all; }
</style>
