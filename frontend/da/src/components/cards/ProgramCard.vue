<template>
    <div class="bg-gray-100 rounded-xl border border-gray-300 p-6">
        <div class="flex items-center space-x-3 mb-6">
            <div class="p-2 bg-yellow-50 rounded-lg">
                <Briefcase class="h-6 w-6 text-yellow-500" />
            </div>
            <h3 class="text-lg font-semibold text-gray-800">Latest Programs</h3>
        </div>

        <div v-if="programs.length > 0" class="space-y-3">
            <div
                v-for="program in programs"
                :key="program.id"
                class="border border-gray-300 rounded-lg p-3"
            >
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center space-x-2">
                        <ClipboardList class="h-5 w-5 text-green-600" />
                        <h4 class="font-medium text-gray-900 text-sm">{{ program.name }}</h4>
                    </div>
                    <PermissionGuard :permission="['CAN_MANAGE_PROGRAM']">
                        <button
                            class="flex items-center bg-gray-600 hover:bg-gray-700 text-white px-2 py-1 rounded-md text-xs transition-colors"
                        >
                            <Settings class="h-3 w-3 mr-1" />
                            Manage
                        </button>
                    </PermissionGuard>
                </div>

                <div class="grid grid-cols-2 gap-2 text-xs mb-2">
                    <div class="flex items-center space-x-1">
                        <span class="font-semibold text-gray-700">Type:</span>
                        <span class="text-gray-600">{{ program.type || 'N/A' }}</span>
                    </div>
                    <div class="flex items-center space-x-1">
                        <span :class="getStatusClass(program.status)" class="px-2 py-0.5 rounded-full text-[10px] font-medium">
                            {{ program.status || 'N/A' }}
                        </span>
                    </div>
                </div>

                <div class="mt-2">
                    <div class="w-full bg-gray-200 rounded-full h-1.5">
                        <div class="bg-yellow-400 h-1.5 rounded-full" :style="{ width: program.completion + '%' }"></div>
                    </div>
                    <p class="text-xs text-gray-600 mt-1">{{ program.completion }}% Complete</p>
                </div>
            </div>
        </div>

        <!-- Empty State -->
        <div v-else class="text-center py-8">
            <Briefcase class="h-12 w-12 text-gray-300 mx-auto mb-3" />
            <p class="text-sm text-gray-500">No programs available</p>
        </div>
    </div>
</template>

<script setup>
import { Briefcase, ClipboardList, Settings } from 'lucide-vue-next'
import PermissionGuard from '@/components/others/PermissionGuard.vue'

defineProps({
    programs: {
        type: Array,
        default: () => []
    }
})

const getStatusClass = (status) => {
    switch (status) {
        case 'ACTIVE':
            return 'bg-green-100 text-green-800'
        case 'COMPLETED':
            return 'bg-blue-100 text-blue-800'
        case 'PENDING':
            return 'bg-yellow-100 text-yellow-800'
        case 'INACTIVE':
            return 'bg-gray-100 text-gray-800'
        case 'CANCELLED':
            return 'bg-red-100 text-red-800'
        default:
            return 'bg-gray-100 text-gray-800'
    }
}
</script>
