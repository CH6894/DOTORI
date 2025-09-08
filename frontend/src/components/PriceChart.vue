  <template>
  <section class="price-chart-section">
    <div class="chart-container">
      <div class="section-header">
        <h3 class="chart-title">시세 동향</h3>
        <p class="chart-subtitle">실시간 가격 변동을 확인하세요</p>
      </div>
      
      <!-- 기간 선택 버튼 -->
      <!-- <div class="period-selector">
        <button 
          v-for="period in periods" 
          :key="period.value"
          @click="selectPeriod(period.value)"
          :class="['period-btn', { active: selectedPeriod === period.value }]"
        >
          {{ period.label }}
        </button>
      </div> -->
      
      <!-- 로딩 중 -->
      <div v-if="loading" class="loading">
        <p>시세 데이터를 불러오는 중...</p>
      </div>
      
      <!-- 데이터가 없을 때 -->
      <div v-else-if="priceData.length === 0" class="no-data">
        <p>아직 거래 이력이 없습니다.</p>
      </div>
      
      <!-- 차트 영역 -->
      <div class="chart-area" :style="{ display: priceData.length > 0 ? 'block' : 'none' }">
        <div class="chart-wrapper">
          <canvas ref="chartCanvas" width="800" height="300"></canvas>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { fetchPriceHistoryPreset } from '@/api/price'

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

const loadPresetPrice = async () => {
  if (!props.itemCode) return
  loading.value = true
  try {
    console.log('API 호출 시작, itemCode:', props.itemCode)
    const priceHistory = await fetchPriceHistoryPreset(props.itemCode)
    console.log('가격 이력 데이터:', priceHistory)
    console.log('데이터 타입:', typeof priceHistory)
    console.log('데이터 길이:', priceHistory?.length)
    
    // API 응답 데이터를 차트 데이터 형태로 변환
    if (Array.isArray(priceHistory) && priceHistory.length > 0) {
      priceData.value = priceHistory.map(dto => {
        console.log('변환 중인 데이터:', dto)
        return {
          date: new Date(dto.payTime).toLocaleDateString('ko-KR'),
          price: dto.price
        }
      })
      
      console.log('변환된 차트 데이터:', priceData.value)
      console.log('priceData.value.length:', priceData.value.length)
      
      // 데이터가 있으면 차트 초기화 (다음 틱에서 실행)
      if (priceData.value.length > 0) {
        console.log('차트 초기화 시작')
        // DOM 렌더링 완료 후 차트 초기화
        await nextTick()
        initChart()
      } else {
        console.log('변환된 데이터가 비어있음')
      }
    } else {
      console.log('API 응답이 배열이 아니거나 비어있음')
      priceData.value = []
    }
  } catch (e) {
    console.error('가격 이력 로드 실패:', e)
    priceData.value = []
  } finally {
    loading.value = false
  }
}


// 초기 데이터 로드
onMounted(() => { loadPresetPrice() })

// 차트 초기화 함수
const initChart = async () => {
  console.log('initChart 호출됨')
  console.log('chartCanvas.value:', chartCanvas.value)
  console.log('priceData.value.length:', priceData.value.length)
  
  if (!chartCanvas.value) {
    console.log('차트 초기화 실패: 캔버스 없음, 재시도 중...')
    // 캔버스가 없으면 잠시 후 재시도
    setTimeout(() => {
      if (chartCanvas.value) {
        console.log('재시도: 캔버스 발견됨')
        initChart()
      } else {
        console.log('재시도 실패: 여전히 캔버스 없음')
      }
    }, 100)
    return
  }
  
  if (priceData.value.length === 0) {
    console.log('차트 초기화 실패: 데이터 없음')
    return
  }
  
  // 기존 차트 인스턴스 제거
  if (window.chartInstance) {
    console.log('기존 차트 인스턴스 제거')
    window.chartInstance.destroy()
  }
  
  const ctx = chartCanvas.value.getContext('2d')
  console.log('캔버스 컨텍스트:', ctx)
  
  // 데이터를 날짜순으로 정렬
  const sortedData = [...priceData.value].sort((a, b) => {
    return new Date(a.date) - new Date(b.date)
  })
  
  console.log('정렬된 차트 데이터:', sortedData)
  console.log('Chart.js 로딩 시작...')
  
  import('chart.js/auto').then((Chart) => {
    console.log('Chart.js 로딩 완료:', Chart)
    console.log('Chart.default:', Chart.default)
    
    try {
      window.chartInstance = new Chart.default(ctx, {
      type: 'line',
      data: {
        labels: sortedData.map(item => item.date),
        datasets: [{
          label: '가격',
          data: sortedData.map(item => item.price),
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
            min: Math.max(0, Math.min(...sortedData.map(item => item.price)) * 0.9),
            max: Math.max(...sortedData.map(item => item.price)) * 1.1,
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
    console.log('차트 생성 완료:', window.chartInstance)
    } catch (error) {
      console.error('차트 생성 실패:', error)
    }
  }).catch(error => {
    console.error('Chart.js 로딩 실패:', error)
  })
}

watch(() => props.itemCode, () => {
  loadPresetPrice()})
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