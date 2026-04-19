<template>
  <div v-if="record" class="result-display">
    <!-- Sentiment Badge -->
    <div class="sentiment-section">
      <div class="sentiment-badge" :style="{ background: badgeBg, color: badgeColor }">
        <span class="sentiment-emoji">{{ sentimentEmoji }}</span>
        <span class="sentiment-text">{{ sentimentLabel }}</span>
      </div>
      <div class="confidence-text">
        置信度：<strong :style="{ color: badgeColor }">{{ confidenceText }}</strong>
      </div>
    </div>

    <!-- Probability Bars -->
    <div v-if="probs" class="prob-section">
      <div class="prob-title">情感概率分布</div>
      <div class="prob-item">
        <span class="prob-label positive-label">积极 😊</span>
        <el-progress
          :percentage="Math.round((probs.positive || 0) * 100)"
          color="#67C23A"
          :stroke-width="14"
          class="prob-bar"
        />
        <span class="prob-val">{{ ((probs.positive || 0) * 100).toFixed(1) }}%</span>
      </div>
      <div class="prob-item">
        <span class="prob-label neutral-label">中性 😐</span>
        <el-progress
          :percentage="Math.round((probs.neutral || 0) * 100)"
          color="#E6A23C"
          :stroke-width="14"
          class="prob-bar"
        />
        <span class="prob-val">{{ ((probs.neutral || 0) * 100).toFixed(1) }}%</span>
      </div>
      <div class="prob-item">
        <span class="prob-label negative-label">消极 😞</span>
        <el-progress
          :percentage="Math.round((probs.negative || 0) * 100)"
          color="#F56C6C"
          :stroke-width="14"
          class="prob-bar"
        />
        <span class="prob-val">{{ ((probs.negative || 0) * 100).toFixed(1) }}%</span>
      </div>
    </div>

    <el-divider />

    <!-- Detail Info -->
    <div class="detail-section">
      <div class="detail-row">
        <span class="detail-key">分析目标</span>
        <el-tag size="small" type="info">{{ record.target }}</el-tag>
      </div>
      <div class="detail-row">
        <span class="detail-key">推文内容</span>
        <span class="detail-val">{{ record.text }}</span>
      </div>
      <div v-if="record.imageUrl" class="detail-row">
        <span class="detail-key">参考图片</span>
        <el-image
          :src="record.imageUrl"
          style="width:100px;height:75px;border-radius:6px;object-fit:cover"
          fit="cover"
          :preview-src-list="[record.imageUrl]"
        />
      </div>
      <div class="detail-row">
        <span class="detail-key">分析时间</span>
        <span class="detail-val">{{ formatTime(record.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  record: {
    type: Object,
    default: null
  }
})

const COLORS = {
  positive: { bg: '#f0f9eb', color: '#67C23A' },
  neutral: { bg: '#fdf6ec', color: '#E6A23C' },
  negative: { bg: '#fef0f0', color: '#F56C6C' }
}

const sentiment = computed(() => props.record?.sentiment || 'neutral')

const badgeBg = computed(() => COLORS[sentiment.value]?.bg || '#f5f5f5')
const badgeColor = computed(() => COLORS[sentiment.value]?.color || '#909399')

const sentimentEmoji = computed(() => ({
  positive: '😊', neutral: '😐', negative: '😞'
}[sentiment.value] || '😐'))

const sentimentLabel = computed(() => ({
  positive: '积极正面', neutral: '情感中性', negative: '消极负面'
}[sentiment.value] || '未知'))

const confidenceText = computed(() => {
  const c = props.record?.confidence
  return c !== undefined ? `${(c * 100).toFixed(1)}%` : '-'
})

const probs = computed(() => props.record?.probabilities || null)

function formatTime(t) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-'
}
</script>

<style scoped>
.result-display {
  padding: 8px 0;
}

.sentiment-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.sentiment-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 28px;
  border-radius: 12px;
  border: 2px solid currentColor;
}

.sentiment-emoji {
  font-size: 32px;
}

.sentiment-text {
  font-size: 22px;
  font-weight: 700;
}

.confidence-text {
  font-size: 15px;
  color: #606266;
}

.prob-section {
  background: #fafafa;
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 4px;
}

.prob-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

.prob-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.prob-label {
  width: 70px;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
}

.positive-label { color: #67C23A; }
.neutral-label { color: #E6A23C; }
.negative-label { color: #F56C6C; }

.prob-bar {
  flex: 1;
}

.prob-val {
  width: 50px;
  text-align: right;
  font-size: 13px;
  color: #606266;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.detail-key {
  width: 70px;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  flex-shrink: 0;
  padding-top: 2px;
}

.detail-val {
  font-size: 13px;
  color: #303133;
  line-height: 1.6;
  flex: 1;
  word-break: break-word;
}
</style>
