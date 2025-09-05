<template>
  <Teleport to="body">
    <transition name="fade">
      <div
        v-if="modelValue"
        class="modal-backdrop"
        @click.self="onClose"
        aria-modal="true"
        role="dialog"
      >
        <div class="modal">
          <header class="modal-header">
            <h3>배송지 수정</h3>
            <button class="icon-btn" @click="onClose" aria-label="닫기">✕</button>
          </header>

          <div class="modal-body">
            <label class="field">
              <span>이름</span>
              <input v-model="form.receiver" type="text" placeholder="받는 분" />
            </label>

            <label class="field">
              <span>휴대폰 번호</span>
              <input v-model="form.phone" type="tel" placeholder="010-0000-0000" />
            </label>

            <!-- 우편번호 검색 -->
            <div class="field inline">
              <label class="grow">
                <span>우편번호</span>
                <input v-model="form.postcode" type="text" placeholder="00000" readonly />
              </label>
              <button class="btn-outline sm" @click="onSearchPostcode">찾기</button>
            </div>

            <label class="field">
              <span>주소</span>
              <input v-model="form.addr1" type="text" placeholder="기본 주소" readonly />
            </label>

            <label class="field">
              <span>상세 주소</span>
              <input v-model="form.addr2" type="text" placeholder="상세 주소" />
            </label>

            <label class="checkbox">
              <input type="checkbox" v-model="form.isDefault" />
              <span>기본 배송지로 설정</span>
            </label>
          </div>

          <footer class="modal-footer">
            <button class="btn ghost" @click="onClose">취소</button>
            <button class="btn primary" @click="onSave">저장하기</button>
          </footer>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script>
import { reactive, watch, onMounted } from 'vue'

export default {
  name: 'AddressEdit',
  props: {
    modelValue: { type: Boolean, default: false },
    value: {
      type: Object,
      default: () => ({
        receiver: '',
        phone: '',
        postcode: '',
        addr1: '',
        addr2: '',
        isDefault: false,
      }),
    },
  },
  emits: ['update:modelValue', 'save'],
  setup(props, { emit }) {
    const form = reactive({
      receiver: '',
      phone: '',
      postcode: '',
      addr1: '',
      addr2: '',
      isDefault: false,
    })

    const loadFromProps = () => {
      Object.assign(form, props.value || {})
    }

    watch(
      () => props.modelValue,
      (open) => {
        if (open) loadFromProps()
      },
      { immediate: true }
    )

    const onClose = () => emit('update:modelValue', false)

    const onSave = () => {
      if (!form.receiver?.trim()) {
        alert('이름을 입력해주세요.')
        return
      }
      if (!form.phone?.trim()) {
        alert('전화번호를 입력해주세요.')
        return
      }
      if (!form.postcode?.trim()) {
        alert('우편번호를 검색해주세요.')
        return
      }
      if (!form.addr1?.trim()) {
        alert('주소를 검색해주세요.')
        return
      }
      if (!form.addr2?.trim()) {
        alert('상세 주소를 입력해주세요.')
        return
      }

      // ✅ mainAddress 합치기
      const mainAddress = `[${form.postcode}] ${form.addr1} ${form.addr2}`

      emit('save', {
        receiver: form.receiver,
        phone: form.phone,
        postcode: form.postcode,
        addr1: form.addr1,
        addr2: form.addr2,
        mainAddress,   // 서버 저장용 전체 주소
        isDefault: form.isDefault,
      })

      onClose()
    }

    // 카카오 우편번호 API 실행
    const onSearchPostcode = () => {
      if (!window.daum) {
        alert('우편번호 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
        return
      }

      var themeObj = {
        searchBgColor: "#FF7A2E", // 검색창 배경색
        queryTextColor: "#FFFFFF", // 검색창 글자색
        outlineColor: "#FF7A2E" // 테두리 색상
      }

      new window.daum.Postcode({
        oncomplete: function (data) {
          form.postcode = data.zonecode
          form.addr1 = data.roadAddress || data.jibunAddress

          console.log('주소 검색 결과:', {
            postcode: data.zonecode,
            roadAddress: data.roadAddress,
            jibunAddress: data.jibunAddress,
          })

          setTimeout(() => {
            const detailInput = document.querySelector('input[placeholder="상세 주소"]')
            if (detailInput) detailInput.focus()
          }, 100)
        },
        theme: themeObj,
        width: '100%',
        height: '100%',
      }).open()
    }

    // 다음 우편번호 서비스 스크립트 로드
    onMounted(() => {
      if (!window.daum) {
        const script = document.createElement('script')
        script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js'
        script.async = true
        document.head.appendChild(script)
      }
    })

    return { form, onClose, onSave, onSearchPostcode }
  },
}
</script>

<style scoped>
/* 애니메이션 */
.fade-enter-active, .fade-leave-active { transition: opacity .15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 레이어 */
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.35);
  display: grid; place-items: center;
  z-index: 1000;
}
.modal {
  width: 420px; max-width: calc(100% - 32px);
  background: #fff; border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,.18);
  overflow: hidden;
}

/* 헤더/바디/푸터 */
.modal-header {
  position: relative;
  padding: 10px 16px;
  border-bottom: 1px solid #eee;
  text-align: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
}

.modal-header .icon-btn {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: 0;
  font-size: 18px;
  cursor: pointer;
}
.modal-body { padding: 14px 16px; display: grid; gap: 12px; }

.field { display: grid; gap: 6px; }
.field.inline { display: grid; grid-template-columns: 1fr auto; align-items: end; gap: 8px; }
.field.inline .grow { display: grid; gap: 6px; }
.field span { font-size: 13px; color: #666; }
input[type="text"], input[type="tel"] {
  width: 90%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 10px;
}

.checkbox { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #333; }

.modal-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 12px 16px; border-top: 1px solid #eee; background: #fafafa;
}
.btn { border-radius: 10px; padding: 10px 14px; cursor: pointer; border: 1px solid #e6e6e6; }
.btn.ghost { background: #fff; }
.btn.primary { background: #222; color: #fff; border-color: #222; }
.btn-outline.sm { padding: 8px 10px; border-radius: 10px; border:1px solid #ddd; background:#fff; cursor:pointer; }
</style>