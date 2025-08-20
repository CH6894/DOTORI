<template>
  <section class="price-chart-section">
    <div class="chart-container">
      <div class="section-header">
        <h3 class="chart-title">시세 동향</h3>
        <p class="chart-subtitle">실시간 가격 변동을 확인하세요</p>
      </div>
      
      <div class="chart-area">
        <div class="chart-wrapper">
          <canvas ref="chartCanvas" width="800" height="300"></canvas>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
/* eslint-disable no-undef */
import { ref, onMounted, watch, computed } from 'vue'

const props = defineProps({
  priceData: {
    type: Array,
    default: () => []
  }
})

const chartCanvas = ref(null)

// Y축 범위 계산
const yAxisRange = computed(() => {
  if (props.priceData.length === 0) return { min: 0, max: 100 }
  
  const prices = props.priceData.map(item => item.price)
  const minPrice = Math.min(...prices)
  const maxPrice = Math.max(...prices)
  const range = maxPrice - minPrice
  const padding = range > 0 ? range * 0.15 : maxPrice * 0.1 // 15% 여백
  
  return {
    min: Math.max(0, minPrice - padding),
    max: maxPrice + padding
  }
})

const initChart = () => {
  if (!chartCanvas.value || props.priceData.length === 0) return
  
  if (window.chartInstance) {
    window.chartInstance.destroy()
  }
  
  const ctx = chartCanvas.value.getContext('2d')
  
  import('chart.js/auto').then((Chart) => {
    window.chartInstance = new Chart.default(ctx, {
      type: 'line',
      data: {
        labels: props.priceData.map(item => item.date),
        datasets: [{
          label: '가격',
          data: props.priceData.map(item => item.price),
          borderColor: '#FC703C',
          borderWidth: 3,
          pointRadius: 4,
          pointBackgroundColor: '#FC703C',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverRadius: 8,
          pointHoverBackgroundColor: '#FC703C',
          pointHoverBorderColor: '#fff',
          pointHoverBorderWidth: 3,
          tension: 0,
          fill: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: {
          duration: 2500,
          easing: 'easeInOutCubic'
        },
        interaction: {
          intersect: false,
          mode: 'index'
        },
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'rgba(252, 112, 60, 0.95)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: '#FC703C',
            borderWidth: 2,
            cornerRadius: 12,
            padding: 12,
            callbacks: {
              label: function(context) {
                return `${context.parsed.y.toLocaleString()}원`
              }
            }
          }
        },
        scales: {
          x: {
            display: false
          },
          y: {
            min: yAxisRange.value.min,
            max: yAxisRange.value.max,
            grid: {
              color: 'rgba(252, 112, 60, 0.08)',
              lineWidth: 1
            },
            border: {
              display: false
            },
            ticks: {
              color: '#8892b0',
              font: {
                size: 12,
                weight: '500'
              },
              padding: 16,
              maxTicksLimit: 6,
              callback: function(value) {
                return (value / 1000).toFixed(0) + 'K'
              }
            }
          }
        }
      }
    })
  })
}

onMounted(() => {
  initChart()
})

watch(() => props.priceData, () => {
  initChart()
}, { deep: true })
</script>

<style scoped>
.price-chart-section {
  padding: 10px 20px;
}

.chart-container {
  max-width: 1280px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 50px;
}

.chart-title {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #FC703C 0%, #FF8A50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.chart-subtitle {
  font-size: 16px;
  color: #8892b0;
  margin: 0;
  font-weight: 500;
}

.chart-area {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 20px 40px rgba(252, 112, 60, 0.1),
    0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.chart-wrapper {
  position: relative;
  height: 300px;
}

.chart-area canvas {
  width: 100% !important;
  height: 100% !important;
  border-radius: 12px;
}

@media (max-width: 768px) {
  .price-chart-section {
    padding: 40px 16px;
  }
  
  .chart-area {
    padding: 24px;
    border-radius: 16px;
  }
  
  .chart-title {
    font-size: 24px;
  }
  
  .chart-subtitle {
    font-size: 14px;
  }
  
  .section-header {
    margin-bottom: 32px;
  }
}
</style>