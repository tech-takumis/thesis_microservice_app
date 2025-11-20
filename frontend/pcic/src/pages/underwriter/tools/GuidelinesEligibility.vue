<template>
  <AuthenticatedLayout
    :navigation="underwriterNavigation"
    role-title="Underwriter Portal"
    page-title="Guidelines Eligibility"
  >
    <!-- Header -->
    <template #header>
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-gray-900">Eligibility Guidelines</h2>
          <p class="mt-1 text-sm text-gray-600">
            Comprehensive eligibility criteria for rice crop insurance coverage
          </p>
        </div>
        <div class="flex items-center space-x-3">
          <!-- Eligibility Checker -->
          <button
            @click="showEligibilityChecker = true"
            class="inline-flex items-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition"
          >
            <CheckSquare class="w-4 h-4 mr-2" />
            Eligibility Checker
          </button>
        </div>
      </div>
    </template>

    <!-- Main Content -->
    <div class="space-y-8">
      
      <!-- Quick Eligibility Overview -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <div v-for="stat in eligibilityStats" :key="stat.title" class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div :class="stat.iconBg" class="rounded-md p-3">
              <component :is="stat.icon" :class="stat.iconColor" class="w-6 h-6" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">{{ stat.title }}</dt>
                <dd class="text-lg font-medium text-gray-900">{{ stat.value }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Eligibility Sections -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
        
        <!-- Farmer Eligibility -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-semibold text-gray-900 flex items-center">
              <User class="w-5 h-5 mr-2 text-green-600" />
              Farmer Eligibility Requirements
            </h3>
          </div>
          <div class="p-6">
            <div class="space-y-4">
              <div v-for="requirement in farmerRequirements" :key="requirement.id" class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <div :class="requirement.type === 'required' ? 'bg-red-100 text-red-600' : 'bg-blue-100 text-blue-600'" class="w-6 h-6 rounded-full flex items-center justify-center">
                    <component :is="requirement.type === 'required' ? 'X' : 'Info'" class="w-4 h-4" />
                  </div>
                </div>
                <div class="ml-3">
                  <p class="text-sm font-medium text-gray-900">{{ requirement.title }}</p>
                  <p class="text-sm text-gray-600">{{ requirement.description }}</p>
                  <div v-if="requirement.details" class="mt-2">
                    <ul class="text-xs text-gray-500 space-y-1">
                      <li v-for="detail in requirement.details" :key="detail" class="flex items-center">
                        <div class="w-1 h-1 bg-gray-400 rounded-full mr-2"></div>
                        {{ detail }}
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Land/Crop Eligibility -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-semibold text-gray-900 flex items-center">
              <Sprout class="w-5 h-5 mr-2 text-green-600" />
              Land & Crop Requirements
            </h3>
          </div>
          <div class="p-6">
            <div class="space-y-4">
              <div v-for="requirement in cropRequirements" :key="requirement.id" class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <div :class="requirement.type === 'required' ? 'bg-red-100 text-red-600' : 'bg-blue-100 text-blue-600'" class="w-6 h-6 rounded-full flex items-center justify-center">
                    <component :is="requirement.type === 'required' ? 'X' : 'Info'" class="w-4 h-4" />
                  </div>
                </div>
                <div class="ml-3">
                  <p class="text-sm font-medium text-gray-900">{{ requirement.title }}</p>
                  <p class="text-sm text-gray-600">{{ requirement.description }}</p>
                  <div v-if="requirement.details" class="mt-2">
                    <ul class="text-xs text-gray-500 space-y-1">
                      <li v-for="detail in requirement.details" :key="detail" class="flex items-center">
                        <div class="w-1 h-1 bg-gray-400 rounded-full mr-2"></div>
                        {{ detail }}
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Coverage Limits & Exclusions -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center">
            <Shield class="w-5 h-5 mr-2 text-green-600" />
            Coverage Limits & Exclusions
          </h3>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <!-- Coverage Limits -->
            <div>
              <h4 class="text-md font-semibold text-gray-800 mb-4">Coverage Limits</h4>
              <div class="space-y-4">
                <div v-for="limit in coverageLimits" :key="limit.type" class="border rounded-lg p-4">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-sm font-medium text-gray-900">{{ limit.type }}</span>
                    <span class="text-lg font-bold text-green-600">{{ limit.amount }}</span>
                  </div>
                  <p class="text-xs text-gray-600">{{ limit.description }}</p>
                  <div v-if="limit.conditions" class="mt-2">
                    <p class="text-xs font-medium text-gray-700">Conditions:</p>
                    <ul class="text-xs text-gray-500 mt-1 space-y-1">
                      <li v-for="condition in limit.conditions" :key="condition" class="flex items-center">
                        <div class="w-1 h-1 bg-gray-400 rounded-full mr-2"></div>
                        {{ condition }}
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Exclusions -->
            <div>
              <h4 class="text-md font-semibold text-gray-800 mb-4">Coverage Exclusions</h4>
              <div class="space-y-3">
                <div v-for="exclusion in coverageExclusions" :key="exclusion.id" class="flex items-start p-3 bg-red-50 border border-red-200 rounded-md">
                  <AlertTriangle class="w-4 h-4 text-red-500 mt-0.5 mr-3 flex-shrink-0" />
                  <div>
                    <p class="text-sm font-medium text-red-800">{{ exclusion.title }}</p>
                    <p class="text-xs text-red-700 mt-1">{{ exclusion.description }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Regional Eligibility Map -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center">
            <MapPin class="w-5 h-5 mr-2 text-green-600" />
            Regional Eligibility Coverage
          </h3>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div v-for="region in eligibleRegions" :key="region.name" class="border rounded-lg p-4">
              <div class="flex items-center justify-between mb-3">
                <h4 class="font-semibold text-gray-800">{{ region.name }}</h4>
                <span :class="region.statusColor" class="px-2 py-1 text-xs font-medium rounded-full">
                  {{ region.status }}
                </span>
              </div>
              <p class="text-sm text-gray-600 mb-3">{{ region.description }}</p>
              <div class="space-y-2">
                <div class="text-xs text-gray-500">
                  <strong>Provinces:</strong> {{ region.provinces.join(', ') }}
                </div>
                <div class="text-xs text-gray-500">
                  <strong>Risk Level:</strong> {{ region.riskLevel }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Eligibility Checker Modal -->
    <div v-if="showEligibilityChecker" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="closeModal">
      <div class="relative top-20 mx-auto p-5 border w-11/12 max-w-lg shadow-lg rounded-md bg-white" @click.stop>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900">Quick Eligibility Checker</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
            <X class="w-6 h-6" />
          </button>
        </div>
        
        <div class="space-y-4">
          <div v-for="(question, index) in eligibilityQuestions" :key="index">
            <label class="block text-sm font-medium text-gray-700 mb-2">{{ question.question }}</label>
            <div class="space-y-2">
              <label v-for="option in question.options" :key="option.value" class="flex items-center">
                <input
                  v-model="checkerAnswers[index]"
                  :value="option.value"
                  type="radio"
                  class="h-4 w-4 text-green-600 focus:ring-green-500 border-gray-300"
                />
                <span class="ml-2 text-sm text-gray-900">{{ option.label }}</span>
              </label>
            </div>
          </div>
          
          <div class="flex justify-end space-x-3 pt-4 border-t">
            <button
              @click="closeModal"
              class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              @click="checkEligibility"
              class="px-4 py-2 bg-green-600 text-white rounded-md text-sm font-medium hover:bg-green-700"
            >
              Check Eligibility
            </button>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'
import { 
  CheckSquare,
  Download,
  User,
  Sprout,
  Shield,
  MapPin,
  AlertTriangle,
  X,
  Info,
  Users,
  FileText,
  Calendar,
  TrendingUp
} from 'lucide-vue-next'

// Navigation
const underwriterNavigation = UNDERWRITER_NAVIGATION

// Modal state
const showEligibilityChecker = ref(false)
const checkerAnswers = ref([])

// Eligibility stats
const eligibilityStats = ref([
  {
    title: 'Eligible Farmers',
    value: '95%',
    icon: Users,
    iconBg: 'bg-green-100',
    iconColor: 'text-green-600'
  },
  {
    title: 'Required Documents',
    value: '8',
    icon: FileText,
    iconBg: 'bg-blue-100',
    iconColor: 'text-blue-600'
  },
  {
    title: 'Processing Days',
    value: '7-14',
    icon: Calendar,
    iconBg: 'bg-yellow-100',
    iconColor: 'text-yellow-600'
  },
  {
    title: 'Success Rate',
    value: '87%',
    icon: TrendingUp,
    iconBg: 'bg-purple-100',
    iconColor: 'text-purple-600'
  }
])

// Farmer requirements
const farmerRequirements = ref([
  {
    id: 1,
    type: 'required',
    title: 'Filipino Citizenship',
    description: 'Must be a Filipino citizen or corporation with Filipino majority ownership',
    details: ['Valid government-issued ID', 'Birth certificate or naturalization papers']
  },
  {
    id: 2,
    type: 'required',
    title: 'Age Requirement',
    description: 'Must be at least 18 years old and not more than 65 years old',
    details: ['For senior farmers (65+), special provisions apply', 'Guardian representation for minors']
  },
  {
    id: 3,
    type: 'required',
    title: 'Farming Experience',
    description: 'Minimum of 2 years rice farming experience or training certification',
    details: ['Certificate from agricultural training', 'Affidavit of farming experience', 'Barangay certification']
  },
  {
    id: 4,
    type: 'preferred',
    title: 'RSBSA Registration',
    description: 'Registered in the Registry System for Basic Sectors in Agriculture',
    details: ['RSBSA ID or certificate', 'Updated farmer profile', 'Verified by Municipal Agriculture Office']
  },
  {
    id: 5,
    type: 'required',
    title: 'No Previous Fraud',
    description: 'No history of fraudulent insurance claims or criminal conviction related to agriculture',
    details: ['NBI clearance', 'Barangay clearance', 'Previous insurance claim history']
  }
])

// Crop requirements
const cropRequirements = ref([
  {
    id: 1,
    type: 'required',
    title: 'Land Ownership/Tenancy',
    description: 'Must have legal right to cultivate the land (owner, tenant, or lessee)',
    details: ['Land title or tax declaration', 'Lease agreement or contract', 'Certificate of Land Ownership Award (CLOA)']
  },
  {
    id: 2,
    type: 'required',
    title: 'Farm Size Limits',
    description: 'Minimum 0.5 hectares, maximum 5 hectares per farmer',
    details: ['Surveyed farm area', 'GPS coordinates verification', 'Municipal verification']
  },
  {
    id: 3,
    type: 'required',
    title: 'Rice Variety',
    description: 'Must plant PCIC-approved rice varieties',
    details: ['PhilRice certified varieties', 'NSIC-approved cultivars', 'Hybrid varieties from authorized dealers']
  },
  {
    id: 4,
    type: 'preferred',
    title: 'Irrigation Access',
    description: 'Irrigated or rainfed areas with adequate water supply',
    details: ['Irrigation association membership', 'Water source verification', 'Seasonal water availability']
  },
  {
    id: 5,
    type: 'required',
    title: 'Planting Schedule',
    description: 'Must follow recommended planting calendar for the region',
    details: ['DA regional planting schedule', 'Climate-appropriate timing', 'Seasonal advisories compliance']
  }
])

// Coverage limits
const coverageLimits = ref([
  {
    type: 'Per Hectare Coverage',
    amount: '₱75,000',
    description: 'Maximum insurable amount per hectare based on production cost',
    conditions: [
      'Covers 85% of total production cost',
      'Includes seeds, fertilizer, labor costs',
      'Subject to regional cost validation'
    ]
  },
  {
    type: 'Per Farmer Limit',
    amount: '₱375,000',
    description: 'Total maximum coverage per individual farmer (5 hectares)',
    conditions: [
      'Applies to all enrolled parcels',
      'Cannot exceed 5 hectares total',
      'Requires separate applications per parcel'
    ]
  },
  {
    type: 'Deductible Amount',
    amount: '10%',
    description: 'Minimum loss threshold before claim payment',
    conditions: [
      'Applies to all types of losses',
      'Calculated from total insured amount',
      'No payment for losses below threshold'
    ]
  }
])

// Coverage exclusions
const coverageExclusions = ref([
  {
    id: 1,
    title: 'War and Civil Unrest',
    description: 'Damages caused by war, rebellion, or civil commotion'
  },
  {
    id: 2,
    title: 'Nuclear Incidents',
    description: 'Losses from nuclear accidents or radioactive contamination'
  },
  {
    id: 3,
    title: 'Poor Farming Practices',
    description: 'Losses due to negligent or improper cultivation methods'
  },
  {
    id: 4,
    title: 'Pre-existing Conditions',
    description: 'Crop damage or disease present before policy inception'
  },
  {
    id: 5,
    title: 'Theft and Malicious Acts',
    description: 'Losses from theft, vandalism, or intentional destruction'
  },
  {
    id: 6,
    title: 'Market Price Fluctuations',
    description: 'Financial losses due to changes in market prices'
  }
])

// Eligible regions
const eligibleRegions = ref([
  {
    name: 'Luzon Regions',
    status: 'Full Coverage',
    statusColor: 'bg-green-100 text-green-800',
    description: 'Complete insurance coverage available for all rice-growing provinces',
    provinces: ['Nueva Ecija', 'Bulacan', 'Tarlac', 'Pangasinan', 'Isabela'],
    riskLevel: 'Low to Medium'
  },
  {
    name: 'Visayas Regions',
    status: 'Selective Coverage',
    statusColor: 'bg-yellow-100 text-yellow-800',
    description: 'Coverage available with additional requirements in high-risk areas',
    provinces: ['Iloilo', 'Negros Occidental', 'Cebu', 'Bohol', 'Leyte'],
    riskLevel: 'Medium to High'
  },
  {
    name: 'Mindanao Regions',
    status: 'Limited Coverage',
    statusColor: 'bg-orange-100 text-orange-800',
    description: 'Coverage available in selected low-risk municipalities only',
    provinces: ['Cotabato', 'Bukidnon', 'Agusan del Norte', 'Davao del Norte'],
    riskLevel: 'High (Selective)'
  }
])

// Eligibility checker questions
const eligibilityQuestions = ref([
  {
    question: 'Are you a Filipino citizen?',
    options: [
      { value: 'yes', label: 'Yes' },
      { value: 'no', label: 'No' }
    ]
  },
  {
    question: 'What is your age?',
    options: [
      { value: '18-65', label: '18-65 years old' },
      { value: 'under18', label: 'Under 18' },
      { value: 'over65', label: 'Over 65' }
    ]
  },
  {
    question: 'How many years of rice farming experience do you have?',
    options: [
      { value: '2plus', label: '2 years or more' },
      { value: '1-2', label: '1-2 years' },
      { value: 'none', label: 'No experience' }
    ]
  },
  {
    question: 'What is the size of your rice farm?',
    options: [
      { value: '0.5-5', label: '0.5 to 5 hectares' },
      { value: 'under0.5', label: 'Less than 0.5 hectares' },
      { value: 'over5', label: 'More than 5 hectares' }
    ]
  }
])

// Methods
const closeModal = () => {
  showEligibilityChecker.value = false
  checkerAnswers.value = []
}

const checkEligibility = () => {
  const answers = checkerAnswers.value
  let eligible = true
  let issues = []
  
  // Check citizenship
  if (answers[0] !== 'yes') {
    eligible = false
    issues.push('Must be a Filipino citizen')
  }
  
  // Check age
  if (answers[1] === 'under18') {
    eligible = false
    issues.push('Must be at least 18 years old')
  }
  
  // Check experience
  if (answers[2] === 'none') {
    eligible = false
    issues.push('Minimum 2 years farming experience required')
  }
  
  // Check farm size
  if (answers[3] === 'under0.5' || answers[3] === 'over5') {
    eligible = false
    issues.push('Farm size must be between 0.5 to 5 hectares')
  }
  
  // Show results
  if (eligible) {
    alert('✅ Congratulations! You meet the basic eligibility requirements for rice crop insurance.')
  } else {
    alert(`❌ Sorry, you do not meet the following requirements:\n\n${issues.join('\n')}`)
  }
  
  closeModal()
}

const exportGuidelines = () => {
  // This would typically generate and download a PDF
  alert('Guidelines PDF export functionality would be implemented here')
}
</script>

<style scoped>
/* Custom styles for better visual hierarchy */
.prose {
  color: #374151;
  line-height: 1.6;
}

/* Modal animation */
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.3s;
}
.modal-enter, .modal-leave-to {
  opacity: 0;
}
</style>