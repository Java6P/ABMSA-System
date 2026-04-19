<template>
  <div class="sentiment-chart-wrap">
    <div ref="chartRef" class="pie-chart" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const chartRef = ref(null)
let chart = null

onMounted(() => {
  chart = echarts.init(chartRef.value)
  renderChart()
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})

watch(() => props.data, renderChart, { deep: true })

function renderChart() {
  if (!chart) return
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['38%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        color: ['#67C23A', '#E6A23C', '#F56C6C'],
        data: props.data.length > 0
          ? props.data
          : [
              { name: '积极', value: 0 },
              { name: '中性', value: 0 },
              { name: '消极', value: 0 }
            ],
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.4)'
          }
        }
      }
    ]
  })
}
</script>

<style scoped>
.sentiment-chart-wrap {
  width: 100%;
}
.pie-chart {
  width: 100%;
  height: 260px;
}
</style>
