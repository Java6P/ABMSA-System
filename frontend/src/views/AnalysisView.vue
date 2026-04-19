<template>
  <div class="analysis-view">
    <el-row :gutter="24">
      <!-- Input Panel -->
      <el-col :span="10">
        <el-card class="input-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><ChatDotRound /></el-icon>
              <span>情感分析</span>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
          >
            <el-form-item label="分析目标（Aspect）" prop="target">
              <el-input
                v-model="form.target"
                placeholder="例：iPhone 15 Pro、服务态度"
                clearable
              />
            </el-form-item>

            <el-form-item label="推文内容" prop="text">
              <el-input
                v-model="form.text"
                type="textarea"
                :rows="5"
                placeholder="请输入需要分析情感的推文内容..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="图片 URL（可选）">
              <el-input
                v-model="form.imageUrl"
                placeholder="https://example.com/image.jpg"
                clearable
              >
                <template #prepend><el-icon><Picture /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="或上传本地图片（可选）">
              <el-upload
                class="img-uploader"
                :show-file-list="false"
                accept="image/*"
                :before-upload="handleImageUpload"
                action="#"
                :auto-upload="false"
              >
                <el-button size="small" plain>
                  <el-icon><Upload /></el-icon> 选择图片
                </el-button>
                <span v-if="uploadedFileName" class="upload-hint">{{ uploadedFileName }}</span>
              </el-upload>
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="analyze-btn"
              :loading="loading"
              @click="handleAnalyze"
            >
              <el-icon><MagicStick /></el-icon>
              开始分析
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- Result Panel -->
      <el-col :span="14">
        <el-card class="result-card" shadow="never" style="min-height: 480px;">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>分析结果</span>
            </div>
          </template>

          <div v-if="!result && !loading" class="empty-result">
            <el-empty description="请输入内容并点击「开始分析」" :image-size="100" />
          </div>

          <div v-if="loading" class="loading-result">
            <el-skeleton :rows="6" animated />
          </div>

          <ResultDisplay v-if="result && !loading" :record="result" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Examples -->
    <el-card class="examples-card" shadow="never" style="margin-top:20px">
      <template #header>
        <div class="card-header">
          <el-icon><Bulb /></el-icon>
          <span>快速示例</span>
        </div>
      </template>
      <div class="examples-grid">
        <el-button
          v-for="(ex, i) in examples"
          :key="i"
          plain
          size="small"
          @click="applyExample(ex)"
        >
          {{ ex.label }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { predict } from '@/api/analysis'
import { ElMessage } from 'element-plus'
import ResultDisplay from '@/components/ResultDisplay.vue'

const formRef = ref(null)
const loading = ref(false)
const result = ref(null)
const uploadedFileName = ref('')

const form = reactive({
  target: '',
  text: '',
  imageUrl: ''
})

const rules = {
  target: [{ required: true, message: '请输入分析目标', trigger: 'blur' }],
  text: [{ required: true, message: '请输入推文内容', trigger: 'blur' }]
}

const examples = [
  { label: '积极示例：手机', target: 'iPhone 15 Pro', text: 'The camera quality on the iPhone 15 Pro is absolutely stunning! Best smartphone experience ever.' },
  { label: '消极示例：服务', target: '客服服务', text: '这家店的客服态度极差，等了半小时还没有人来处理问题，非常失望。' },
  { label: '中性示例：价格', target: '价格', text: 'The price is around $999 which is typical for flagship phones in this category.' }
]

function applyExample(ex) {
  form.target = ex.target
  form.text = ex.text
  form.imageUrl = ''
}

function handleImageUpload(file) {
  uploadedFileName.value = file.name
  const reader = new FileReader()
  reader.onload = (e) => {
    form.imageUrl = e.target.result
  }
  reader.readAsDataURL(file)
  return false
}

async function handleAnalyze() {
  await formRef.value.validate()
  loading.value = true
  result.value = null
  try {
    const data = await predict(form.text, form.target, form.imageUrl || null)
    result.value = {
      target: form.target,
      text: form.text,
      sentiment: data.sentiment || data.label,
      confidence: data.confidence || data.score,
      probabilities: data.probabilities || data.probs || null,
      imageUrl: form.imageUrl || null,
      createdAt: new Date().toISOString()
    }
  } catch (e) {
    // mock result for demo
    result.value = {
      target: form.target,
      text: form.text,
      sentiment: 'positive',
      confidence: 0.87,
      probabilities: { positive: 0.87, neutral: 0.09, negative: 0.04 },
      imageUrl: form.imageUrl || null,
      createdAt: new Date().toISOString()
    }
    ElMessage.warning('后端未连接，显示模拟结果')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.analysis-view {
  padding: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
}

.analyze-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 2px;
  margin-top: 4px;
}

.upload-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.img-uploader {
  display: flex;
  align-items: center;
}

.empty-result {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 380px;
}

.loading-result {
  padding: 20px;
}

.examples-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
