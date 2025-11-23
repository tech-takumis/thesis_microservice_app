<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100/50 flex">
    <!-- Left Column - PCIC Image (Hidden on mobile) -->
    <div class="hidden lg:flex lg:w-1/2 bg-slate-50 items-center justify-center p-8">
      <div class="max-w-md text-center">
        <img
          src="@/assets/pcic.svg"
          alt="Philippine Crop Insurance Corporation Logo"
          class="w-28 h-auto mx-auto mb-6"
        />

        <h1 class="text-3xl font-bold text-slate-900 mb-2">
          Philippine Crop Insurance Corporation
        </h1>
        <p class="text-slate-600 text-lg">
          Staff Portal - Managing crop insurance for Filipino farmers
        </p>
      </div>
    </div>

    <!-- Right Column - Registration Form -->
    <div class="w-full lg:w-1/2 flex items-center justify-center p-8 bg-slate-50 min-h-screen">
      <div class="w-full max-w-2xl bg-white/70 backdrop-blur-sm border border-slate-200/60 rounded-2xl shadow-sm p-8">

        <!-- Logo (mobile & desktop) -->
        <div class="text-center mb-6">
          <h2 class="text-2xl font-light text-slate-900 mb-1 lg:hidden">PCIC Staff Portal</h2>
          <h2 class="text-3xl font-light text-slate-900 mb-1 hidden lg:block tracking-tight">Staff Registration</h2>
          <p class="text-slate-600 text-sm lg:text-base">Create your PCIC staff account</p>
        </div>

        <!-- Error Messages -->
        <div v-if="errors && errors.length > 0" class="mb-6">
          <div class="bg-red-50/50 border border-red-200/60 rounded-xl p-5 backdrop-blur-sm">
            <div class="flex">
              <AlertCircle class="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-900">Please correct the following errors:</h3>
                <ul class="mt-1 list-disc pl-5 text-sm text-red-700/80 space-y-0.5">
                  <li v-for="error in errors" :key="error">{{ error }}</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- Registration Form -->
        <form @submit.prevent="submitRegistration" class="space-y-6">

          <!-- First Name & Last Name -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
            <div>
              <label for="firstName" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                First Name
              </label>
              <input
                id="firstName"
                v-model="form.firstName"
                type="text"
                required
                autocomplete="given-name"
                :disabled="processing"
                placeholder="Enter your first name"
                class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              />
            </div>

            <div>
              <label for="lastName" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                Last Name
              </label>
              <input
                id="lastName"
                v-model="form.lastName"
                type="text"
                required
                autocomplete="family-name"
                :disabled="processing"
                placeholder="Enter your last name"
                class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              />
            </div>
          </div>

          <!-- Email -->
          <div>
            <label for="email" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
              Email Address
            </label>
            <div class="relative">
              <input
                id="email"
                v-model="form.email"
                type="email"
                required
                autocomplete="email"
                :disabled="processing"
                placeholder="Enter your email address"
                class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed pl-10"
              />
              <Mail class="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400" />
            </div>
          </div>

          <!-- Phone Number -->
          <div>
            <label for="phoneNumber" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
              Phone Number
            </label>
            <div class="relative">
              <input
                id="phoneNumber"
                v-model="form.phoneNumber"
                type="tel"
                required
                autocomplete="tel"
                :disabled="processing"
                placeholder="+639123456789"
                pattern="^\+63\d{10}$"
                class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed pl-10"
              />
              <Phone class="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400" />
            </div>
            <p class="mt-1 text-xs text-slate-500">Format: +63 followed by 10 digits</p>
          </div>

          <!-- Address -->
          <div>
            <label for="address" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
              Address
            </label>
            <div class="relative">
              <textarea
                id="address"
                v-model="form.address"
                rows="3"
                required
                autocomplete="street-address"
                :disabled="processing"
                placeholder="Enter your complete address"
                class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium resize-none placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed pl-10 pt-3"
              ></textarea>
              <MapPin class="absolute left-3 top-4 h-5 w-5 text-slate-400" />
            </div>
          </div>

          <!-- Role Selection -->
          <div>
            <label for="role" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
              Role
            </label>
            <div class="relative">
              <select
                id="role"
                v-model="selectedRole"
                required
                :disabled="processing"
                class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed pl-10 appearance-none"
              >
                <option value="" disabled>Select your role</option>
                <option v-for="role in availableRoles" :key="role.name" :value="role.name">
                  {{ role.display }}
                </option>
              </select>
              <Shield class="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400 pointer-events-none" />
              <ChevronDown class="absolute right-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
            </div>
          </div>

          <!-- Password & Confirm Password -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
            <div>
              <label for="password" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                Password
              </label>
              <div class="relative">
                <input
                  id="password"
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  required
                  autocomplete="new-password"
                  :disabled="processing"
                  placeholder="Enter password"
                  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed pl-10 pr-10"
                />
                <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400" />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3 top-1/2 transform -translate-y-1/2"
                  :disabled="processing"
                >
                  <Eye v-if="!showPassword" class="h-5 w-5 text-slate-400 hover:text-slate-600 transition-colors duration-200" />
                  <EyeOff v-else class="h-5 w-5 text-slate-400 hover:text-slate-600 transition-colors duration-200" />
                </button>
              </div>
            </div>

            <div>
              <label for="confirmPassword" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                Confirm Password
              </label>
              <div class="relative">
                <input
                  id="confirmPassword"
                  v-model="confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  required
                  autocomplete="new-password"
                  :disabled="processing"
                  placeholder="Confirm password"
                  :class="{ 'border-red-300 focus:ring-red-500/10 focus:border-red-500': passwordMismatch }"
                  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed pl-10 pr-10"
                />
                <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400" />
                <button
                  type="button"
                  @click="showConfirmPassword = !showConfirmPassword"
                  class="absolute right-3 top-1/2 transform -translate-y-1/2"
                  :disabled="processing"
                >
                  <Eye v-if="!showConfirmPassword" class="h-5 w-5 text-slate-400 hover:text-slate-600 transition-colors duration-200" />
                  <EyeOff v-else class="h-5 w-5 text-slate-400 hover:text-slate-600 transition-colors duration-200" />
                </button>
              </div>
              <p v-if="passwordMismatch" class="mt-1 text-xs text-red-700/80">Passwords do not match</p>
            </div>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="processing || passwordMismatch"
            class="w-full inline-flex justify-center items-center px-6 py-2.5 border border-transparent text-sm font-medium rounded-xl text-white bg-blue-500 hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-none"
          >
            <Loader2 v-if="processing" class="animate-spin h-4 w-4 mr-2" />
            {{ processing ? 'Creating Account...' : 'Create Account' }}
          </button>
        </form>

        <!-- Footer -->
        <div class="mt-6 text-center">
          <p class="text-sm text-slate-600">
            Already have an account?
            <router-link to="/login" class="text-blue-500 hover:text-blue-600 font-medium underline transition-colors duration-200">
              Sign in
            </router-link>
          </p>
        </div>

        <div class="mt-4 text-center text-xs text-slate-500 space-y-1">
          <p>© 2024 Philippine Crop Insurance Corporation. All rights reserved.</p>
          <p class="text-slate-400">Staff Portal Access Only</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import {
  User, Lock, Eye, EyeOff, Loader2, AlertCircle, Mail, Phone, MapPin, Shield, ChevronDown
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Static roles
const availableRoles = [
  { name: 'UNDERWRITER', display: 'Underwriter' },
  { name: 'CLAIMS_ADJUSTMENT_STAFF', display: 'Claims Adjustment Staff' }
]

// Get token from URL query parameter
const token = ref('')

// Form data
const form = ref({
  token: token.value,
  firstName: '',
  lastName: '',
  password: '',
  email: '',
  phoneNumber: '',
  address: '',
  roleNames: []
})

const selectedRole = ref('')
const confirmPassword = ref('')
const processing = ref(false)
const setErrors = ref([])
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const errors = computed(() => setErrors.value)

// Check if passwords match
const passwordMismatch = computed(() => {
  if (!confirmPassword.value) return false
  return form.value.password !== confirmPassword.value
})

// Initialize component
onMounted(() => {
  // Check for token in query params
  token.value = route.query.token || ''

  if (!token.value) {
    // No token provided, redirect to login
    router.push({ name: 'login' })
    return
  }

  console.log('Registration token:', token.value)
})

const submitRegistration = async () => {
  // Clear previous errors
  setErrors.value = []

  // Validate passwords match
  if (form.value.password !== confirmPassword.value) {
    setErrors.value = ['Passwords do not match']
    return
  }

  // Validate role selection
  if (!selectedRole.value) {
    setErrors.value = ['Please select a role']
    return
  }

  // Set rolesId as a Set with the selected role name (not ID)
  form.value.roleNames = [selectedRole.value]
  form.value.token = token.value

  console.log('Submitting registration with form data:', form.value)
  // Call auth store register method
//   await authStore.register(form, setErrors, processing)
}
</script>
