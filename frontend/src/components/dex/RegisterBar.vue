<!-- src/components/dex/RegisterBar.vue -->
<template>
    <section class="register-section">
        <div class="register-container">
            <div class="register-bar">
                <div class="search-input-wrapper">
                    <input type="text" class="register-input" :placeholder="placeholder" v-model="localCode"
                        @keypress.enter="emitRegister" />
                    <button v-if="localCode" class="clear-btn" @click="clear" title="전체 삭제">✕</button>
                </div>
                <button class="register-btn" @click="emitRegister">등록하기</button>
            </div>
        </div>
    </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
    code?: string
    placeholder?: string
}>(), {
    code: '',
    placeholder: '영수증 번호 또는 인증코드를 입력하세요'
})

const emit = defineEmits<{
    (e: 'update:code', v: string): void
    (e: 'register', v: string): void
    (e: 'clear'): void
}>()

const localCode = computed({
    get: () => props.code ?? '',
    set: v => emit('update:code', v)
})

function emitRegister() {
    emit('register', localCode.value)
}
function clear() {
    emit('update:code', '')
    emit('clear')
}
</script>

<style scoped>
.register-section {
    padding: 2rem 0;
}

.register-container {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 2rem;
}

.register-bar {
    display: flex;
    gap: 1rem;
    align-items: center;
    justify-content: center;
}

.search-input-wrapper {
    position: relative;
    max-width: 400px;
    width: 100%;
    display: flex;
    align-items: center;
}

.register-input {
    width: 100%;
    padding: 1rem 3rem 1rem 1.5rem;
    border: 2px solid #EFECC6;
    border-radius: 15px;
    font-size: 14px;
    outline: none;
    transition: all .3s;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, .05);
}

.register-input:focus {
    border-color: #670600;
    box-shadow: 0 4px 12px rgba(103, 6, 0, .1);
}

.register-input::placeholder {
    color: #999;
}

.clear-btn {
    position: absolute;
    right: 12px;
    background: none;
    border: none;
    color: #999;
    font-size: 1.2rem;
    cursor: pointer;
    padding: 8px;
    border-radius: 50%;
    transition: all .3s;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
}

.clear-btn:hover {
    background: rgba(103, 6, 0, .1);
    color: #670600;
    transform: scale(1.1);
}

.register-btn {
    background: #670600;
    color: #fff;
    border: none;
    padding: 16px 20px;
    border-radius: 15px;
    cursor: pointer;
    font-weight: 500;
    font-size: 12px;
    transition: all .3s;
    box-shadow: 0 4px 12px rgba(103, 6, 0, .2);
    white-space: nowrap;
}

.register-btn:hover {
    background: linear-gradient(45deg, #5a0500, #5a0500);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(103, 6, 0, .3);
}

@media (max-width: 768px) {
    .register-container {
        padding: 0 1rem;
    }

    .register-bar {
        flex-direction: column;
        gap: 1rem;
    }

    .search-input-wrapper {
        max-width: none;
        width: 100%;
    }
}
</style>
