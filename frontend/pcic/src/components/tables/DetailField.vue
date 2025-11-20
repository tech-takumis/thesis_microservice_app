<template>
  <div class="detail-field">
    <dt class="text-sm font-medium text-gray-500">{{ label }}</dt>
    <dd class="mt-1 text-sm text-gray-900">
      {{ displayValue }}
    </dd>
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
    type: [String, Number, Boolean, Object],
    default: null
  }
})

const displayValue = computed(() => {
  if (props.value === null || props.value === undefined) {
    return 'N/A'
  }

  if (typeof props.value === 'boolean') {
    return props.value ? 'Yes' : 'No'
  }

  if (typeof props.value === 'object') {
    return JSON.stringify(props.value)
  }

  return props.value
})
</script>

<style scoped>
.detail-field {
  display: flex;
  flex-direction: column;
}
</style>
