import { defineStore } from 'pinia'

export const useOrderStore = defineStore('orders', {
  state: () => ({
    // 주문 요약
    orderSummary: { 
      total: 4, 
      deposit: 1, 
      progress: 1, 
      done: 2 
    },
    
    // 주문 목록 (기존 + 배송 현황용 확장)
    orders: [
      {
        id: 'order1',
        no: 'ORD-2025081801',
        date: '2025.08.18',
        orderDate: '2025년 8월 18일 14:32',
        state: { type: 'progress', text: '진행중' },
        progress: 2, // 0: 주문접수, 1: 결제완료, 2: 상품준비, 3: 배송중, 4: 배송완료
        totalAmount: 1354000,
        shippingFee: 0,
        address: '서울특별시 강남구 테헤란로 123, 456호 (우편번호: 12345)',
        phone: '010-1234-5678',
        recipient: '김다람',
        items: [
          {
            id: 'item1',
            name: '코즈메 켄마 피규어',
            title: '코즈메 켄마 피규어', // 기존 호환성
            price: 59000,
            qty: 1,
            image: 'https://via.placeholder.com/50x50'
          },
          {
            id: 'item2',
            name: '아카자 인형',
            title: '아카자 인형',
            price: 48000,
            qty: 3,
            image: 'https://via.placeholder.com/50x50'
          },
          {
            id: 'item3',
            name: '하츠네 미쿠 피규어',
            title: '하츠네 미쿠 피규어',
            price: 53000,
            qty: 2,
            image: 'https://via.placeholder.com/50x50'
          }
        ],
        trackingInfo: [
          {
            id: 1,
            date: '08월 18일',
            time: '14:32',
            status: '주문 접수',
            detail: '주문이 접수되었습니다.',
            isCurrent: false
          },
          {
            id: 2,
            date: '08월 18일',
            time: '14:45',
            status: '결제 완료',
            detail: '결제가 완료되었습니다.',
            isCurrent: false
          },
          {
            id: 3,
            date: '08월 19일',
            time: '09:20',
            status: '상품 준비중',
            detail: '판매자가 상품을 준비하고 있습니다.',
            isCurrent: true
          }
        ]
      },
      {
        id: 'order2',
        no: 'ORD-2025081702',
        date: '2025.08.17',
        orderDate: '2025년 8월 17일 11:22',
        state: { type: 'done', text: '배송완료' },
        progress: 4,
        totalAmount: 96000,
        shippingFee: 0,
        address: '서울특별시 강남구 테헤란로 123, 456호 (우편번호: 12345)',
        phone: '010-1234-5678',
        recipient: '김다람',
        items: [
          {
            id: 'item4',
            name: '아카자 인형',
            title: '아카자 인형',
            price: 48000,
            qty: 2,
            image: 'https://via.placeholder.com/50x50'
          }
        ],
        trackingInfo: [
          {
            id: 1,
            date: '08월 17일',
            time: '11:22',
            status: '주문 접수',
            detail: '주문이 접수되었습니다.',
            isCurrent: false
          },
          {
            id: 2,
            date: '08월 17일',
            time: '11:35',
            status: '결제 완료',
            detail: '결제가 완료되었습니다.',
            isCurrent: false
          },
          {
            id: 3,
            date: '08월 17일',
            time: '14:20',
            status: '상품 준비완료',
            detail: '상품 준비가 완료되어 배송 준비중입니다.',
            isCurrent: false
          },
          {
            id: 4,
            date: '08월 18일',
            time: '08:30',
            status: '배송 시작',
            detail: '택배사에서 상품을 픽업했습니다.',
            isCurrent: false
          },
          {
            id: 5,
            date: '08월 18일',
            time: '16:45',
            status: '배송 완료',
            detail: '배송이 완료되었습니다.',
            isCurrent: true
          }
        ]
      },
      {
        id: 'order3',
        no: 'ORD-2025081501',
        date: '2025.08.15',
        orderDate: '2025년 8월 15일 16:45',
        state: { type: 'done', text: '배송완료' },
        progress: 4,
        totalAmount: 67000,
        shippingFee: 0,
        address: '서울특별시 강남구 테헤란로 123, 456호 (우편번호: 12345)',
        phone: '010-1234-5678',
        recipient: '김다람',
        items: [
          {
            id: 'item5',
            name: '히나타 소요 피규어',
            title: '히나타 소요 피규어',
            price: 67000,
            qty: 1,
            image: 'https://via.placeholder.com/50x50'
          }
        ],
        trackingInfo: [
          {
            id: 1,
            date: '08월 15일',
            time: '16:45',
            status: '주문 접수',
            detail: '주문이 접수되었습니다.',
            isCurrent: false
          },
          {
            id: 2,
            date: '08월 15일',
            time: '16:58',
            status: '결제 완료',
            detail: '결제가 완료되었습니다.',
            isCurrent: false
          },
          {
            id: 3,
            date: '08월 16일',
            time: '09:30',
            status: '상품 준비완료',
            detail: '상품 준비가 완료되어 배송 준비중입니다.',
            isCurrent: false
          },
          {
            id: 4,
            date: '08월 16일',
            time: '14:20',
            status: '배송 시작',
            detail: '택배사에서 상품을 픽업했습니다.',
            isCurrent: false
          },
          {
            id: 5,
            date: '08월 17일',
            time: '10:15',
            status: '배송 완료',
            detail: '배송이 완료되었습니다.',
            isCurrent: true
          }
        ]
      },
      {
        id: 'order4',
        no: 'ORD-2025081201',
        date: '2025.08.12',
        orderDate: '2025년 8월 12일 10:30',
        state: { type: 'deposit', text: '입금대기' },
        progress: 1,
        totalAmount: 10000,
        shippingFee: 3000,
        address: '서울특별시 강남구 테헤란로 123, 456호 (우편번호: 12345)',
        phone: '010-1234-5678',
        recipient: '김다람',
        items: [
          {
            id: 'item6',
            name: '렌고쿠 코쥬로 키링',
            title: '렌고쿠 코쥬로 키링',
            price: 10000,
            qty: 1,
            image: 'https://via.placeholder.com/50x50'
          }
        ],
        trackingInfo: [
          {
            id: 1,
            date: '08월 12일',
            time: '10:30',
            status: '주문 접수',
            detail: '주문이 접수되었습니다.',
            isCurrent: false
          },
          {
            id: 2,
            date: '08월 12일',
            time: '10:45',
            status: '결제 대기',
            detail: '입금을 기다리고 있습니다.',
            isCurrent: true
          }
        ]
      }
    ],
    
    // 로딩 상태
    loading: false,
    error: null
  }),

  getters: {
    // 기존 게터들
    getOrdersByType: (state) => (type) => {
      return state.orders.filter(order => order.state.type === type)
    },

    // 배송 현황용 게터들
    userOrders: (state) => state.orders,
    
    getOrderById: (state) => (id) => {
      return state.orders.find(order => order.id === id)
    },

    getOrdersByStatus: (state) => (progress) => {
      return state.orders.filter(order => order.progress === progress)
    },

    // 진행 중인 주문들
    ordersInProgress: (state) => {
      return state.orders.filter(order => order.progress < 4)
    },

    // 완료된 주문들
    completedOrders: (state) => {
      return state.orders.filter(order => order.progress === 4)
    }
  },

  actions: {
    // 전체 주문 목록 가져오기
    async fetchUserOrders() {
      this.loading = true
      this.error = null
      
      try {
        // 실제 API 호출
        // const response = await api.orders.getUserOrders()
        // this.orders = response.data
        
        // 현재는 더미 데이터 시뮬레이션
        await new Promise(resolve => setTimeout(resolve, 500))
        console.log('주문 목록 로드 완료:', this.orders.length, '개')
        
      } catch (error) {
        this.error = error.message
        console.error('주문 목록 로드 실패:', error)
      } finally {
        this.loading = false
      }
    },

    // 특정 주문 상세 정보 가져오기
    async fetchOrderDetail(orderId) {
      try {
        // 실제 API 호출
        // const response = await api.orders.getOrderDetail(orderId)
        
        // 현재는 더미 데이터 시뮬레이션
        await new Promise(resolve => setTimeout(resolve, 200))
        
        const order = this.getOrderById(orderId)
        if (order) {
          // 추적 정보 업데이트 시뮬레이션
          console.log('주문 상세 정보 업데이트:', orderId)
        }
        
      } catch (error) {
        this.error = error.message
        console.error('주문 상세 정보 로드 실패:', error)
      }
    },

    // 주문 상태 업데이트
    updateOrderStatus(orderId, newProgress, trackingInfo) {
      const orderIndex = this.orders.findIndex(order => order.id === orderId)
      if (orderIndex !== -1) {
        this.orders[orderIndex].progress = newProgress
        if (trackingInfo) {
          this.orders[orderIndex].trackingInfo.push(trackingInfo)
        }
      }
    },

    // 주문 요약 정보 새로고침
    refreshOrderSummary() {
      const total = this.orders.length
      const deposit = this.orders.filter(order => order.state.type === 'deposit').length
      const progress = this.orders.filter(order => order.state.type === 'progress').length
      const done = this.orders.filter(order => order.state.type === 'done').length
      
      this.orderSummary = { total, deposit, progress, done }
    }
  }
})