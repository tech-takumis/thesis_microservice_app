<template>
    <div :class="className">
        <label class="block text-sm font-medium text-green-600 mb-2">{{ label }}</label>

        <div class="w-full bg-green-100 p-3">
            <p class="text-sm text-gray-900">{{ displayValue }}</p>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
    label: {
        type: String,
        required: true
    },
    value: {
        type: [String, Number, Boolean],
        default: null
    },
    class: {
        type: String,
        default: ''
    }
})

const displayValue = computed(() => {
    if (props.value === null || props.value === undefined || props.value === '') {
        return 'N/A'
    }
    return props.value
})

const className = computed(() => {
    const base = 'flex flex-col'
    return [base, props.class].filter(Boolean).join(' ')
})
</script>
