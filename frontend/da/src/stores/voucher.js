import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "@/lib/axios";
import QRCode from 'qrcode';
import { Html5QrcodeScanner } from "html5-qrcode";

export const useVoucherStore = defineStore("voucher", () => {
    // State
    const vouchers = ref([])
    const currentVoucher = ref(null)
    const loading = ref(false)
    const error = ref(null)
    const qrCodeImage = ref(null)
    const scanner = ref(null)

    // Computed
    const allVouchers = computed(() => vouchers.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value != null)
    const errorMessage = computed(() => error.value)

    // Actions
    const createVoucher = async (voucherData) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.post('/api/v1/vouchers', voucherData)
            const newVoucher = response.data
            
            vouchers.value.unshift(newVoucher)

            return {
                success: true,
                message: "Voucher created successfully",
                data: newVoucher
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to create voucher"
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

    const claimVoucher = async (code, userId) => {
        try {
            loading.value = true
            error.value = null

            const params = new URLSearchParams({
                code: code,
                userId: userId
            })

            const response = await axios.post(`/api/v1/vouchers/claim?${params}`)
            const claimedVoucher = response.data

            // Update voucher in local state if it exists
            const index = vouchers.value.findIndex(v => v.code === code)
            if (index !== -1) {
                vouchers.value[index] = claimedVoucher
            }

            return {
                success: true,
                message: "Voucher claimed successfully",
                data: claimedVoucher
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to claim voucher"
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

    const getVouchersByOwner = async (ownerUserId) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/vouchers/owner/${ownerUserId}`)
            const ownerVouchers = Array.isArray(response.data) ? response.data : []

            return {
                success: true,
                message: "Vouchers retrieved successfully",
                data: ownerVouchers
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve vouchers by owner"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: []
            }
        } finally {
            loading.value = false
        }
    }

    const getVouchersByStatus = async (status) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/vouchers/status/${status}`)
            const statusVouchers = Array.isArray(response.data) ? response.data : []

            return {
                success: true,
                message: "Vouchers retrieved successfully",
                data: statusVouchers
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve vouchers by status"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: []
            }
        } finally {
            loading.value = false
        }
    }

    const getVoucherByCode = async (code) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/vouchers/code/${code}`)
            // Backend returns Optional, so data might be null
            const voucher = response.data || null

            return {
                success: true,
                message: voucher ? "Voucher found" : "Voucher not found",
                data: voucher
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve voucher by code"
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

    const getVoucherById = async (id) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/vouchers/${id}`)
            // Backend returns Optional, so data might be null
            const voucher = response.data || null
            
            if (voucher) {
                currentVoucher.value = voucher
            }

            return {
                success: true,
                message: voucher ? "Voucher retrieved successfully" : "Voucher not found",
                data: voucher
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve voucher"
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

    const updateVoucher = async (id, voucherData) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.put(`/api/v1/vouchers/${id}`, voucherData)
            const updatedVoucher = response.data

            // Update voucher in the vouchers array
            const index = vouchers.value.findIndex(voucher => voucher.id === id)
            if (index !== -1) {
                vouchers.value[index] = updatedVoucher
            }

            // Update current voucher if it matches
            if (currentVoucher.value && currentVoucher.value.id === id) {
                currentVoucher.value = updatedVoucher
            }

            return {
                success: true,
                message: "Voucher updated successfully",
                data: updatedVoucher
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to update voucher"
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

    const deleteVoucher = async (id) => {
        try {
            loading.value = true
            error.value = null

            await axios.delete(`/api/v1/vouchers/${id}`)

            // Remove voucher from the vouchers array
            vouchers.value = vouchers.value.filter(voucher => voucher.id !== id)

            // Clear current voucher if it matches
            if (currentVoucher.value && currentVoucher.value.id === id) {
                currentVoucher.value = null
            }

            return {
                success: true,
                message: "Voucher deleted successfully"
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to delete voucher"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage
            }
        } finally {
            loading.value = false
        }
    }

    // Utility method to load all vouchers (not in controller but useful)
    const getAllVouchers = async () => {
        try {
            loading.value = true
            error.value = null

            // Fetch vouchers from all statuses and combine them
            const statuses = ['ISSUED', 'CLAIMED', 'EXPIRED', 'CANCELLED']
            const allVoucherPromises = statuses.map(status =>
                axios.get(`/api/v1/vouchers/status/${status}`)
            )

            const responses = await Promise.all(allVoucherPromises)
            const allVoucherData = responses.flatMap(response =>
                Array.isArray(response.data) ? response.data : []
            )

            vouchers.value = allVoucherData

            return {
                success: true,
                message: "Vouchers retrieved successfully",
                data: allVoucherData
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve all vouchers"
            error.value = errorMessage
            vouchers.value = []
            return {
                success: false,
                message: errorMessage,
                data: []
            }
        } finally {
            loading.value = false
        }
    }

    // QR Code Generation
    const generateQRCode = async (voucherCode, options = {}) => {
        try {
            const defaultOptions = {
                width: 256,
                height: 256,
                margin: 2,
                color: {
                    dark: '#000000',
                    light: '#FFFFFF'
                }
            }

            const qrOptions = { ...defaultOptions, ...options }
            
            // Generate QR code as data URL
            const qrCodeDataURL = await QRCode.toDataURL(voucherCode, qrOptions)
            qrCodeImage.value = qrCodeDataURL

            return {
                success: true,
                message: "QR code generated successfully",
                data: qrCodeDataURL
            }
        } catch (err) {
            const errorMessage = err.message || "Failed to generate QR code"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        }
    }

    // QR Code Generation as Canvas
    const generateQRCodeCanvas = async (canvasElement, voucherCode, options = {}) => {
        try {
            const defaultOptions = {
                width: 256,
                margin: 2,
                color: {
                    dark: '#000000',
                    light: '#FFFFFF'
                }
            }

            const qrOptions = { ...defaultOptions, ...options }
            
            // Generate QR code directly to canvas
            await QRCode.toCanvas(canvasElement, voucherCode, qrOptions)

            return {
                success: true,
                message: "QR code generated to canvas successfully"
            }
        } catch (err) {
            const errorMessage = err.message || "Failed to generate QR code to canvas"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage
            }
        }
    }

    // QR Code Scanner Setup
    const setupQRScanner = (elementId, config = {}) => {
        try {
            const defaultConfig = {
                fps: 10,
                qrbox: 250,
                aspectRatio: 1.0
            }

            const scannerConfig = { ...defaultConfig, ...config }

            const onScanSuccess = async (decodedText, decodedResult) => {
                console.log(`QR Code detected: ${decodedText}`, decodedResult)
                
                // Automatically claim the voucher when QR code is scanned
                const result = await claimVoucher(decodedText, getCurrentUserId())
                
                if (result.success) {
                    // Stop scanner after successful claim
                    stopQRScanner()
                    return {
                        success: true,
                        message: "Voucher claimed successfully from QR scan",
                        data: result.data
                    }
                } else {
                    return {
                        success: false,
                        message: result.message
                    }
                }
            }

            const onScanError = (errorMessage) => {
                // Handle scan errors (optional - can be ignored for continuous scanning)
                console.warn(`QR Scan error: ${errorMessage}`)
            }

            scanner.value = new Html5QrcodeScanner(
                elementId,
                scannerConfig,
                false // verbose logging
            )

            scanner.value.render(onScanSuccess, onScanError)

            return {
                success: true,
                message: "QR scanner initialized successfully"
            }
        } catch (err) {
            const errorMessage = err.message || "Failed to setup QR scanner"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage
            }
        }
    }

    // Stop QR Scanner
    const stopQRScanner = async () => {
        try {
            if (scanner.value) {
                await scanner.value.clear()
                scanner.value = null
            }
            return {
                success: true,
                message: "QR scanner stopped successfully"
            }
        } catch (err) {
            const errorMessage = err.message || "Failed to stop QR scanner"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage
            }
        }
    }

    // Scan QR Code from Image File
    const scanQRFromFile = async (imageFile) => {
        try {
            const html5QrCode = new Html5Qrcode("temp-scan-area")
            
            const decodedText = await html5QrCode.scanFile(imageFile, true)
            
            // Automatically claim the voucher
            const result = await claimVoucher(decodedText, getCurrentUserId())
            
            return {
                success: true,
                message: "QR code scanned from file successfully",
                voucherCode: decodedText,
                claimResult: result
            }
        } catch (err) {
            const errorMessage = err.message || "Failed to scan QR code from file"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        }
    }

    // Helper function to get current user ID (you may need to implement this based on your auth system)
    const getCurrentUserId = () => {
        // This should return the current authenticated user's ID
        // You might get this from auth store or JWT token
        // For now, returning null - implement based on your auth system
        return null
    }

    return {
        // State
        vouchers,
        currentVoucher,
        loading,
        error,
        qrCodeImage,
        scanner,

        // Computed
        allVouchers,
        isLoading,
        hasError,
        errorMessage,

        // Actions
        createVoucher,
        claimVoucher,
        getVouchersByOwner,
        getVouchersByStatus,
        getVoucherByCode,
        getVoucherById,
        updateVoucher,
        deleteVoucher,
        getAllVouchers,

        // QR Code Functions
        generateQRCode,
        generateQRCodeCanvas,
        setupQRScanner,
        stopQRScanner,
        scanQRFromFile
    }
});