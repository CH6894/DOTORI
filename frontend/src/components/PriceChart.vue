<template>
  <section class="price-chart-section">
    <div class="chart-container">
      <div class="section-header">
        <h3 class="chart-title">시세 동향</h3>
        <p class="chart-subtitle">실시간 가격 변동을 확인하세요</p>
      </div>
      
      <!-- 기간 선택 버튼 -->
      <div class="period-selector">
        <button 
          v-for="period in periods" 
          :key="period.value"
          @click="selectPeriod(period.value)"
          :class="['period-btn', { active: selectedPeriod === period.value }]"
        >
          {{ period.label }}
        </button>
      </div>
      
      <!-- 데이터가 없을 때 -->
      <div v-if="priceData.length === 0" class="no-data">
        <p>아직 거래 이력이 없습니다.</p>
      </div>
      
      <!-- 차트 영역 -->
      <div v-else class="chart-area">
        <div class="chart-wrapper">
          <canvas ref="chartCanvas" width="800" height="300"></canvas>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { fetchPriceHistoryByPeriod } from '@/api/items'

const props = defineProps({
  itemCode: {
    type: String,
    required: true
  }
})

const chartCanvas = ref(null)
const priceData = ref([])
const selectedPeriod = ref('1M') // 기본값 1개월
const loading = ref(false)

// 기간 옵션
const periods = [
  { value: '1M', label: '1개월' },
  { value: '3M', label: '3개월' },
  { value: '6M', label: '6개월' },
  { value: '1Y', label: '1년' }
]

// 기간 선택
const selectPeriod = async (period) => {
  if (selectedPeriod.value === period) return
  
  console.log('=== 시세 데이터 로드 시작 ===')
  console.log('itemCode:', props.itemCode)
  console.log('period:', period)
  
  selectedPeriod.value = period
  loading.value = true
  
  try {
    const priceHistory = await fetchPriceHistoryByPeriod(props.itemCode, period)
    console.log('받은 시세 데이터:', priceHistory)
    
    // Chart.js에 맞는 형식으로 변환
    priceData.value = priceHistory.map(item => ({
      date: new Date(item.payTime).toLocaleDateString('ko-KR'),
      price: item.price
    }))
    
    console.log('변환된 차트 데이터:', priceData.value)
    
    // 차트 다시 그리기
    initChart()
  } catch (error) {
    console.error('시세 데이터 로드 실패:', error)
    priceData.value = []
  } finally {
    loading.value = false
  }
}

// 초기 데이터 로드
onMounted(() => {
  selectPeriod(selectedPeriod.value)
})

// 차트 초기화 함수
const initChart = () => {
  if (!chartCanvas.value || priceData.value.length === 0) return
  
  // 기존 차트 인스턴스 제거
  if (window.chartInstance) {
    window.chartInstance.destroy()
  }
  
  const ctx = chartCanvas.value.getContext('2d')
  
  import('chart.js/auto').then((Chart) => {
    window.chartInstance = new Chart.default(ctx, {
      type: 'line',
      data: {
        labels: priceData.value.map(item => item.date),
        datasets: [{
          label: '가격',
          data: priceData.value.map(item => item.price),
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
            min: Math.max(0, Math.min(...priceData.value.map(item => item.price)) * 0.9),
            max: Math.max(...priceData.value.map(item => item.price)) * 1.1,
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

watch(() => props.itemCode, () => {
  selectPeriod(selectedPeriod.value)
})
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

.period-selector {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 30px;
}

.period-btn {
  padding: 8px 16px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.2s ease;
}

.period-btn:hover {
  border-color: #FC703C;
  color: #FC703C;
}

.period-btn.active {
  background: #FC703C;
  border-color: #FC703C;
  color: white;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #FC703C;
}
</style>