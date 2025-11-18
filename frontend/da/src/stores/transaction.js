import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/lib/axios'

export const useTransactionStore = defineStore('transaction', () => {
    // State
    const transactions = ref([])
    const loading = ref(false)
    const error = ref(null)

    // Computed
    const allTransactions = computed(() => transactions.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value != null)
    const errorMessage = computed(() => error.value)

    // Helper function to map backend data to frontend format
    const mapTransactionData = (backendTransaction) => ({
        id: backendTransaction.transactionId,
        type: backendTransaction.type,
        description: backendTransaction.name,
        amount: backendTransaction.amount,
        date: backendTransaction.date,
        status: backendTransaction.status,
        positive: backendTransaction.positive
    })

    // Helper function to map frontend data to backend format
    const mapToBackendFormat = (frontendData) => ({
        name: frontendData.description,
        type: frontendData.type,
        amount: frontendData.amount,
        date: frontendData.date.includes('T') ? frontendData.date : frontendData.date + 'T00:00:00',
        status: frontendData.status || 'PENDING',
        isPositive: frontendData.type === 'INCOME'
    })

    // Actions
    const createTransaction = async (data) => {
        loading.value = true
        error.value = null

        try {
            const payload = mapToBackendFormat(data)
            const response = await axios.post('/api/v1/agriculture/transactions', payload)

            if (response.data && response.data.transactionId) {
                const transaction = mapTransactionData(response.data)
                transactions.value.unshift(transaction)

                return {
                    success: true,
                    message: "Transaction created successfully",
                    data: transaction
                }
            }

            return {
                success: false,
                message: "No transaction ID received from server",
                data: null
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Network error"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        } finally {
            loading.value = false
        }
    }

    const getAllTransactions = async () => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get('/api/v1/agriculture/transactions')

            // Handle array response directly
            const rawTransactions = Array.isArray(response.data) ? response.data : []
            const mappedTransactions = rawTransactions.map(mapTransactionData)

            transactions.value = mappedTransactions

            return {
                success: true,
                message: "Transactions retrieved successfully",
                data: mappedTransactions
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message
            error.value = errorMessage
            transactions.value = []
            return {
                success: false,
                message: errorMessage,
                data: []
            }
        } finally {
            loading.value = false
        }
    }

    const deleteTransaction = async (id) => {
        try {
            loading.value = true
            error.value = null

            await axios.delete(`/api/v1/agriculture/transactions/${id}`)
            transactions.value = transactions.value.filter(t => t.id !== id)

            return { success: true, message: "Transaction deleted successfully" }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message
            error.value = errorMessage
            return { success: false, message: errorMessage }
        } finally {
            loading.value = false
        }
    }

    const updateTransaction = async (id, data) => {
        try {
            loading.value = true
            error.value = null

            const backendData = mapToBackendFormat(data)
            const response = await axios.put(`/api/v1/agriculture/transactions/${id}`, backendData)
            const mappedTransaction = mapTransactionData(response.data)

            const index = transactions.value.findIndex(t => t.id === id)
            if (index !== -1) {
                transactions.value[index] = mappedTransaction
            }

            return { success: true, message: "Transaction updated successfully", data: mappedTransaction }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message
            error.value = errorMessage
            return { success: false, message: errorMessage, data: null }
        } finally {
            loading.value = false
        }
    }

    return {
        // State
        transactions,
        loading,
        error,

        // Computed
        allTransactions,
        isLoading,
        hasError,
        errorMessage,

        // Actions
        createTransaction,
        getAllTransactions,
        updateTransaction,
        deleteTransaction
    }
})