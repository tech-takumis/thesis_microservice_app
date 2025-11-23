<template>
  <span :class="badgeClasses" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
    {{ status }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
    status: {
        type: String,
        required: true
    }
})

// Computed
const badgeClasses = computed(() => {
    const baseClasses = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium'

    switch (props.status.toLowerCase()) {
        case 'pending':
            return 'bg-yellow-100 text-yellow-800'
        case 'approved':
            return 'bg-green-100 text-green-800'
        case 'schedule_assigned_for_inspection':
            return 'bg-blue-100 text-blue-800'
        case 'inspection_completed':
            return 'bg-indigo-100 text-indigo-800'
        case 'verified':
            return 'bg-teal-100 text-teal-800'
        case 'claimed_issued':
            return 'bg-purple-100 text-purple-800'
        case 'policy_issued':
            return 'bg-emerald-100 text-emerald-800'
        case 'rejected':
            return 'bg-red-100 text-red-800'
        case 'submitted':
            return 'bg-blue-100 text-blue-800'
        default:
            return 'bg-gray-100 text-gray-800'
    }
})
</script>