<template>
  <div class="dashboard-view">
    <!-- Toolbar -->
    <div class="dashboard-toolbar">
      <el-select
        v-model="selectedKeywordId"
        placeholder="全部关键词"
        clearable
        style="width:200px"
        @change="loadAll"
      >
        <el-option
          v-for="kw in keywords"
          :key="kw.id"
          :label="kw.keyword"
          :value="kw.id"
        />
      </el-select>

      <el-select v-model="period" style="width:120px" @change="loadTrend">
        <el-option label="最近7天" value="7d" />
        <el-option label="最近30天" value="30d" />
        <el-option label="最近90天" value="90d" />
      </el-select>

      <el-switch
        v-model="autoRefresh"
        active-text="自动刷新"
        inactive-text="手动"
        @change="toggleAutoRefresh"
      />
      <el-button :icon="Refresh" circle @click="loadAll" />
    </div>

    <!-- Stat Cards -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card class="stat-card" shadow="hover" :style="{ borderTop: `4px solid ${card.color}` }">
          <div class="stat-body">
            <div class="stat-icon" :style="{ background: card.color + '20', color: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 1 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><div class="chart-title">情感分布（饼图）</div></template>
          <div ref="pieChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><div class="chart-title">情感趋势（折线图）</div></template>
          <div ref="lineChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 2 -->
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><div class="chart-title">关键词分析量 TOP 5</div></template>
          <div ref="barChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-card shadow="never">
          <template #header><div class="chart-title">目标对比</div></template>
          <div ref="compareChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-card shadow="never">
          <template #header><div class="chart-title">高频词云（Top 10）</div></template>
          <div ref="wordCloudRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { listKeywords } from '@/api/keyword'
import { getOverview, getSentimentTrend, getKeywordStats, getTargetCompare, getWordCloud } from '@/api/dashboard'
import { Refresh, TrendCharts, Histogram, ChatDotRound, Monitor } from '@element-plus/icons-vue'

const selectedKeywordId = ref(null)
const period = ref('7d')
const autoRefresh = ref(false)
const keywords = ref([])
let refreshTimer = null

// Chart refs
const pieChartRef = ref(null)
const lineChartRef = ref(null)
const barChartRef = ref(null)
const compareChartRef = ref(null)
const wordCloudRef = ref(null)

let pieChart, lineChart, barChart, compareChart, wordCloudChart

// Stat cards
const overview = reactive({
  total: 0, positiveRate: 0, negativeRate: 0, keywordsCount: 0
})

const statCards = computed(() => [
  { label: '总分析次数', value: overview.total, color: '#409EFF', icon: 'TrendCharts' },
  { label: '积极占比', value: overview.total ? `${(overview.positiveRate * 100).toFixed(1)}%` : '0%', color: '#67C23A', icon: 'ChatDotRound' },
  { label: '消极占比', value: overview.total ? `${(overview.negativeRate * 100).toFixed(1)}%` : '0%', color: '#F56C6C', icon: 'Histogram' },
  { label: '监测关键词', value: overview.keywordsCount, color: '#E6A23C', icon: 'Monitor' }
])

onMounted(async () => {
  initCharts()
  await loadKeywords()
  await loadAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  clearAutoRefresh()
  window.removeEventListener('resize', handleResize)
  ;[pieChart, lineChart, barChart, compareChart, wordCloudChart].forEach(c => c?.dispose())
})

function initCharts() {
  pieChart = echarts.init(pieChartRef.value)
  lineChart = echarts.init(lineChartRef.value)
  barChart = echarts.init(barChartRef.value)
  compareChart = echarts.init(compareChartRef.value)
  wordCloudChart = echarts.init(wordCloudRef.value)
}

function handleResize() {
  ;[pieChart, lineChart, barChart, compareChart, wordCloudChart].forEach(c => c?.resize())
}

async function loadKeywords() {
  try {
    const data = await listKeywords()
    keywords.value = Array.isArray(data) ? data : (data?.list || [])
    overview.keywordsCount = keywords.value.length
  } catch {
    keywords.value = []
  }
}

async function loadAll() {
  await Promise.all([loadOverview(), loadTrend(), loadKeywordStats(), loadTargetCompare(), loadWordCloud()])
}

async function loadOverview() {
  try {
    const data = await getOverview()
    overview.total = data.total || 0
    overview.positiveRate = data.positiveRate || 0
    overview.negativeRate = data.negativeRate || 0
    overview.keywordsCount = data.keywordsCount || keywords.value.length
    renderPieChart(data.distribution || mockDistribution())
  } catch {
    const mock = mockDistribution()
    overview.total = mock.reduce((a, b) => a + b.value, 0)
    overview.positiveRate = (mock[0].value / overview.total)
    overview.negativeRate = (mock[2].value / overview.total)
    renderPieChart(mock)
  }
}

async function loadTrend() {
  try {
    const data = await getSentimentTrend(selectedKeywordId.value, period.value)
    renderLineChart(data)
  } catch {
    renderLineChart(mockTrendData())
  }
}

async function loadKeywordStats() {
  try {
    const data = await getKeywordStats(selectedKeywordId.value || 0)
    renderBarChart(Array.isArray(data) ? data : mockKwStats())
  } catch {
    renderBarChart(mockKwStats())
  }
}

async function loadTargetCompare() {
  try {
    const data = await getTargetCompare(selectedKeywordId.value)
    renderCompareChart(Array.isArray(data) ? data : mockTargetCompare())
  } catch {
    renderCompareChart(mockTargetCompare())
  }
}

async function loadWordCloud() {
  try {
    const data = await getWordCloud(selectedKeywordId.value)
    renderWordCloud(Array.isArray(data) ? data : mockWordCloud())
  } catch {
    renderWordCloud(mockWordCloud())
  }
}

function renderPieChart(data) {
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      data: data,
      color: ['#67C23A', '#E6A23C', '#F56C6C'],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
      label: { formatter: '{b}: {d}%' }
    }]
  })
}

function renderLineChart(data) {
  const dates = data.dates || data.map?.(d => d.date) || []
  const positive = data.positive || data.map?.(d => d.positive) || []
  const neutral = data.neutral || data.map?.(d => d.neutral) || []
  const negative = data.negative || data.map?.(d => d.negative) || []

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['积极', '中性', '消极'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      { name: '积极', type: 'line', smooth: true, data: positive, itemStyle: { color: '#67C23A' }, areaStyle: { opacity: 0.2 } },
      { name: '中性', type: 'line', smooth: true, data: neutral, itemStyle: { color: '#E6A23C' }, areaStyle: { opacity: 0.2 } },
      { name: '消极', type: 'line', smooth: true, data: negative, itemStyle: { color: '#F56C6C' }, areaStyle: { opacity: 0.2 } }
    ]
  })
}

function renderBarChart(data) {
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '6%', bottom: '5%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { width: 80, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: data.map(d => d.count),
      itemStyle: { color: '#409EFF', borderRadius: [0, 4, 4, 0] },
      label: { show: true, position: 'right' }
    }]
  })
}

function renderCompareChart(data) {
  compareChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['积极', '中性', '消极'], bottom: 0, textStyle: { fontSize: 11 } },
    grid: { left: '3%', right: '4%', bottom: '20%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.target), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [
      { name: '积极', type: 'bar', stack: 'total', data: data.map(d => d.positive || 0), itemStyle: { color: '#67C23A' } },
      { name: '中性', type: 'bar', stack: 'total', data: data.map(d => d.neutral || 0), itemStyle: { color: '#E6A23C' } },
      { name: '消极', type: 'bar', stack: 'total', data: data.map(d => d.negative || 0), itemStyle: { color: '#F56C6C' } }
    ]
  })
}

function renderWordCloud(data) {
  const sorted = [...data].sort((a, b) => b.value - a.value).slice(0, 10)
  wordCloudChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '6%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map(d => d.name || d.word) },
    series: [{
      type: 'bar',
      data: sorted.map(d => d.value),
      itemStyle: {
        color: (params) => {
          const colors = ['#409EFF','#67C23A','#E6A23C','#F56C6C','#909399','#6f7ad3','#13c2c2','#eb2f96','#fa8c16','#a0d911']
          return colors[params.dataIndex % colors.length]
        },
        borderRadius: [0, 4, 4, 0]
      }
    }]
  })
}

function toggleAutoRefresh(val) {
  if (val) {
    refreshTimer = setInterval(loadAll, 30000)
  } else {
    clearAutoRefresh()
  }
}

function clearAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// Mock data generators
function mockDistribution() {
  return [
    { name: '积极', value: 342 },
    { name: '中性', value: 215 },
    { name: '消极', value: 143 }
  ]
}

function mockTrendData() {
  const days = []
  const positive = [], neutral = [], negative = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    days.push(`${d.getMonth() + 1}/${d.getDate()}`)
    positive.push(Math.floor(Math.random() * 50 + 30))
    neutral.push(Math.floor(Math.random() * 30 + 15))
    negative.push(Math.floor(Math.random() * 25 + 10))
  }
  return { dates: days, positive, neutral, negative }
}

function mockKwStats() {
  return [
    { name: 'iPhone 15', count: 238 },
    { name: 'Tesla Model 3', count: 184 },
    { name: 'ChatGPT', count: 156 },
    { name: '华为手机', count: 122 },
    { name: 'MacBook Pro', count: 98 }
  ]
}

function mockTargetCompare() {
  return [
    { target: '摄像头', positive: 80, neutral: 30, negative: 20 },
    { target: '电池', positive: 50, neutral: 40, negative: 60 },
    { target: '设计', positive: 90, neutral: 25, negative: 15 },
    { target: '价格', positive: 30, neutral: 55, negative: 75 }
  ]
}

function mockWordCloud() {
  return [
    { name: 'camera', value: 156 }, { name: 'battery', value: 134 },
    { name: 'design', value: 112 }, { name: 'price', value: 98 },
    { name: 'performance', value: 87 }, { name: 'display', value: 76 },
    { name: '摄像头', value: 65 }, { name: '续航', value: 54 },
    { name: '外观', value: 43 }, { name: '价格', value: 38 }
  ]
}
</script>

<style scoped>
.dashboard-view { padding: 0; }

.dashboard-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card { }

.stat-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content { flex: 1; }

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 280px;
  width: 100%;
}
</style>
