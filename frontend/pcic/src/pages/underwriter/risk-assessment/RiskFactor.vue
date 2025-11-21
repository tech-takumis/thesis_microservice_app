<template>
  <AuthenticatedLayout
    :navigation="underwriterNavigation"
    role-title="Underwriter Portal"
    page-title="Risk Factors"
  >
    <!-- Header -->
    <template #header>
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-semibold text-green-600">Risk Factors Assessment</h2>
          <p class="mt-1 text-sm text-gray-600">
            Comprehensive risk evaluation for crop insurance underwriting
          </p>
        </div>
      </div>
    </template>

    <!-- Main Content -->
    <div class="h-full flex flex-col space-y-4">
      <!-- Risk Overview Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div class="bg-gray-100 rounded-xl p-6 border border-gray-300">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-red-100 rounded-lg flex items-center justify-center">
                <AlertTriangle class="w-5 h-5 text-red-600" />
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">High Risk Applications</p>
              <p class="text-2xl font-bold text-gray-900">{{ riskSummary.highRisk }}</p>
              <p class="text-xs text-red-600">{{ riskSummary.highRiskPercentage }}% of total</p>
            </div>
          </div>
        </div>
        
        <div class="bg-gray-100 rounded-xl p-6 border border-gray-300">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-yellow-100 rounded-lg flex items-center justify-center">
                <WarningIcon class="w-5 h-5 text-yellow-600" />
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">Medium Risk Applications</p>
              <p class="text-2xl font-bold text-gray-900">{{ riskSummary.mediumRisk }}</p>
              <p class="text-xs text-yellow-600">{{ riskSummary.mediumRiskPercentage }}% of total</p>
            </div>
          </div>
        </div>
        
        <div class="bg-gray-100 rounded-xl p-6 border border-gray-300">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                <ShieldCheck class="w-5 h-5 text-green-600" />
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">Low Risk Applications</p>
              <p class="text-2xl font-bold text-gray-900">{{ riskSummary.lowRisk }}</p>
              <p class="text-xs text-green-600">{{ riskSummary.lowRiskPercentage }}% of total</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        <!-- Risk Factor Categories -->
        <div class="lg:col-span-2 space-y-4">

          <!-- Crop & Agricultural Risk -->
          <div class="bg-gray-100 rounded-xl border border-gray-300">
            <div class="px-6 py-4 border-b border-gray-300">
              <h3 class="text-lg font-medium text-gray-900 flex items-center">
                <Sprout class="w-5 h-5 mr-2 text-green-600" />
                Crop & Agricultural Risk Factors
              </h3>
            </div>
            <div class="p-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="factor in cropRiskFactors" :key="factor.id" class="border border-gray-300 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-sm font-medium text-gray-800">{{ factor.name }}</span>
                    <span 
                      class="px-2 py-1 text-xs font-medium rounded-full"
                      :class="getRiskBadgeClass(factor.riskLevel)"
                    >
                      {{ factor.riskLevel }}
                    </span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2 mb-2">
                    <div 
                      class="h-2 rounded-full"
                      :class="getRiskBarClass(factor.riskLevel)"
                      :style="{ width: factor.score + '%' }"
                    ></div>
                  </div>
                  <p class="text-xs text-gray-600">{{ factor.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Financial & Market Risk -->
          <div class="bg-gray-100 rounded-xl border border-gray-300">
            <div class="px-6 py-4 border-b border-gray-300">
              <h3 class="text-lg font-medium text-gray-900 flex items-center">
                <DollarSign class="w-5 h-5 mr-2 text-green-600" />
                Financial & Market Risk Factors
              </h3>
            </div>
            <div class="p-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="factor in financialRiskFactors" :key="factor.id" class="border border-gray-300 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-sm font-medium text-gray-800">{{ factor.name }}</span>
                    <span 
                      class="px-2 py-1 text-xs font-medium rounded-full"
                      :class="getRiskBadgeClass(factor.riskLevel)"
                    >
                      {{ factor.riskLevel }}
                    </span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2 mb-2">
                    <div 
                      class="h-2 rounded-full"
                      :class="getRiskBarClass(factor.riskLevel)"
                      :style="{ width: factor.score + '%' }"
                    ></div>
                  </div>
                  <p class="text-xs text-gray-700">{{ factor.description }}</p>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- Risk Assessment Tools & Guidelines -->
        <div class="space-y-4">
          <!-- Risk Guidelines -->
          <div class="bg-green-600 rounded-xl border border-gray-300">
            <div class="px-6 py-4 border-b border-green-300">
              <h3 class="text-lg font-medium text-white flex items-center">
                <BookOpen class="w-5 h-5 mr-2 text-green-300" />
                Risk Assessment Guidelines
              </h3>
            </div>
            <div class="p-6">
              <div class="space-y-4">
                <div v-for="guideline in riskGuidelines" :key="guideline.id" class="border-l-4 pl-4" :class="guideline.borderColor">
                  <h4 class="text-sm font-semibold text-gray-800">{{ guideline.title }}</h4>
                  <p class="text-xs text-white mt-1">{{ guideline.description }}</p>
                  <div class="mt-2">
                    <span class="text-xs font-medium" :class="guideline.textColor">
                      Score Range: {{ guideline.scoreRange }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Recent Risk Alerts -->
          <div class="bg-gray-100 rounded-xl border border-gray-300">
            <div class="px-6 py-4 border-b border-gray-300">
              <h3 class="text-lg font-medium text-gray-800 flex items-center">
                <Bell class="w-5 h-5 mr-2 text-red-600" />
                Risk Alerts
              </h3>
            </div>
            <div class="p-6">
              <div class="space-y-3">
                <div v-for="alert in riskAlerts" :key="alert.id" class="flex items-start space-x-3">
                  <div class="flex-shrink-0">
                    <div class="w-2 h-2 rounded-full mt-2" :class="alert.severityColor"></div>
                  </div>
                  <div class="min-w-0 flex-1">
                    <p class="text-sm font-medium text-black">{{ alert.title }}</p>
                    <p class="text-xs text-gray-700">{{ alert.message }}</p>
                    <p class="text-xs text-black mt-1">{{ formatDate(alert.timestamp) }}</p>
                  </div>
                </div>
                
                <div v-if="riskAlerts.length === 0" class="text-center py-4">
                  <p class="text-sm text-gray-500">No recent risk alerts</p>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'
import { 
  AlertTriangle, 
  AlertTriangle as WarningIcon, 
  ShieldCheck, 
  TrendingUp,
  Cloud,
  Sprout as SeedlingIcon,
  DollarSign,
  Calculator,
  BookOpen,
  Bell,
  Sprout
} from 'lucide-vue-next'

// Navigation
const underwriterNavigation = UNDERWRITER_NAVIGATION

// Loading state
const loading = ref(false)

// Risk Summary Data
const riskSummary = ref({
  highRisk: 23,
  highRiskPercentage: 15.3,
  mediumRisk: 87,
  mediumRiskPercentage: 58.0,
  lowRisk: 40,
  lowRiskPercentage: 26.7,
  averageScore: 62,
  scoreTrend: 3.2
})

// Weather & Climate Risk Factors
const weatherRiskFactors = ref([
  {
    id: 1,
    name: 'Typhoon Exposure',
    riskLevel: 'High',
    score: 85,
    description: 'Historical typhoon frequency and intensity in the region'
  },
  {
    id: 2,
    name: 'Drought Risk',
    riskLevel: 'Medium',
    score: 65,
    description: 'Seasonal rainfall patterns and water availability'
  },
  {
    id: 3,
    name: 'Flood Susceptibility',
    riskLevel: 'High',
    score: 78,
    description: 'Topography and flood history assessment'
  },
  {
    id: 4,
    name: 'Temperature Extremes',
    riskLevel: 'Medium',
    score: 58,
    description: 'Heat stress and cold damage potential'
  },
  {
    id: 5,
    name: 'Hail Damage',
    riskLevel: 'Low',
    score: 25,
    description: 'Regional hail occurrence and crop damage history'
  },
  {
    id: 6,
    name: 'Wind Damage',
    riskLevel: 'Medium',
    score: 62,
    description: 'Strong wind patterns affecting crop stability'
  }
])

// Crop & Agricultural Risk Factors
const cropRiskFactors = ref([
  {
    id: 1,
    name: 'Pest & Disease Pressure',
    riskLevel: 'High',
    score: 82,
    description: 'Historical pest outbreaks and disease incidence'
  },
  {
    id: 2,
    name: 'Soil Quality',
    riskLevel: 'Medium',
    score: 68,
    description: 'Soil fertility, pH levels, and nutrient availability'
  },
  {
    id: 3,
    name: 'Variety Susceptibility',
    riskLevel: 'Medium',
    score: 55,
    description: 'Crop variety resistance to local conditions'
  },
  {
    id: 4,
    name: 'Planting Method Risk',
    riskLevel: 'Low',
    score: 35,
    description: 'Direct seeding vs transplanting success rates'
  },
  {
    id: 5,
    name: 'Irrigation Reliability',
    riskLevel: 'High',
    score: 75,
    description: 'Water source dependability and infrastructure'
  },
  {
    id: 6,
    name: 'Fertilizer Availability',
    riskLevel: 'Medium',
    score: 48,
    description: 'Input supply chain stability and cost factors'
  }
])

// Financial & Market Risk Factors
const financialRiskFactors = ref([
  {
    id: 1,
    name: 'Market Price Volatility',
    riskLevel: 'High',
    score: 88,
    description: 'Historical price fluctuations and market stability'
  },
  {
    id: 2,
    name: 'Credit Risk',
    riskLevel: 'Medium',
    score: 52,
    description: 'Farmer financial capacity and loan default rates'
  },
  {
    id: 3,
    name: 'Insurance History',
    riskLevel: 'Low',
    score: 28,
    description: 'Previous claims and payment reliability'
  },
  {
    id: 4,
    name: 'Farm Size Viability',
    riskLevel: 'Medium',
    score: 61,
    description: 'Economic scale and production efficiency'
  },
  {
    id: 5,
    name: 'Transportation Costs',
    riskLevel: 'Medium',
    score: 57,
    description: 'Market access and logistics expenses'
  },
  {
    id: 6,
    name: 'Storage & Post-Harvest',
    riskLevel: 'High',
    score: 73,
    description: 'Infrastructure for crop preservation and quality'
  }
])

// Risk Calculator
const riskCalculator = reactive({
  region: '',
  cropType: '',
  season: '',
  farmSize: '',
  calculatedScore: null
})

// Regions
const regions = ref([
  'National Capital Region',
  'Cordillera Administrative Region', 
  'Region I - Ilocos Region',
  'Region II - Cagayan Valley',
  'Region III - Central Luzon',
  'Region IV-A - CALABARZON',
  'Region IV-B - MIMAROPA',
  'Region V - Bicol Region',
  'Region VI - Western Visayas',
  'Region VII - Central Visayas',
  'Region VIII - Eastern Visayas',
  'Region IX - Zamboanga Peninsula',
  'Region X - Northern Mindanao',
  'Region XI - Davao Region',
  'Region XII - SOCCSKSARGEN',
  'Region XIII - Caraga',
  'Autonomous Region in Muslim Mindanao'
])

// Risk Guidelines
const riskGuidelines = ref([
  {
    id: 1,
    title: 'Low Risk (0-40)',
    description: 'Applications with minimal risk factors. Standard coverage and premium rates apply.',
    scoreRange: '0-40',
    borderColor: 'border-green-400',
    textColor: 'text-green-300'
  },
  {
    id: 2,
    title: 'Medium Risk (41-70)',
    description: 'Moderate risk applications requiring additional assessment and possible premium adjustments.',
    scoreRange: '41-70',
    borderColor: 'border-yellow-400',
    textColor: 'text-yellow-400'
  },
  {
    id: 3,
    title: 'High Risk (71-100)',
    description: 'High-risk applications requiring detailed evaluation, enhanced monitoring, or coverage limitations.',
    scoreRange: '71-100',
    borderColor: 'border-red-400',
    textColor: 'text-red-700'
  }
])

// Risk Alerts
const riskAlerts = ref([
  {
    id: 2,
    title: 'Pest Outbreak Warning',
    message: 'Brown planthopper infestation reported in Central Luzon rice areas.',
    timestamp: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    severityColor: 'bg-orange-300'
  },
  {
    id: 3,
    title: 'Drought Risk Update',
    message: 'Below-normal rainfall expected in Mindanao regions for next 30 days.',
    timestamp: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    severityColor: 'bg-yellow-300'
  }
])

// Methods
const getRiskBadgeClass = (riskLevel) => {
  switch (riskLevel?.toLowerCase()) {
    case 'high':
      return 'bg-red-100 text-red-800'
    case 'medium':
      return 'bg-yellow-100 text-yellow-800'
    case 'low':
      return 'bg-green-100 text-green-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getRiskBarClass = (riskLevel) => {
  switch (riskLevel?.toLowerCase()) {
    case 'high':
      return 'bg-red-600'
    case 'medium':
      return 'bg-yellow-400'
    case 'low':
      return 'bg-green-600'
    default:
      return 'bg-gray-300'
  }
}

const calculateRisk = () => {
  if (!riskCalculator.region || !riskCalculator.cropType || !riskCalculator.season) {
    riskCalculator.calculatedScore = null
    return
  }

  // Simple risk calculation algorithm
  let score = 50 // Base score

  // Region risk adjustments
  const regionRiskMap = {
    'Region VIII - Eastern Visayas': 15,
    'Region XIII - Caraga': 12,
    'Region V - Bicol Region': 10,
    'Region III - Central Luzon': 8,
    'National Capital Region': -5
  }
  score += regionRiskMap[riskCalculator.region] || 5

  // Crop type adjustments
  const cropRiskMap = {
    'rice': 5,
    'corn': 8,
    'coconut': 3,
    'sugarcane': 6
  }
  score += cropRiskMap[riskCalculator.cropType] || 0

  // Season adjustments
  if (riskCalculator.season === 'wet') {
    score += 10 // Higher risk during wet season
  } else {
    score += 5
  }

  // Farm size adjustments
  const farmSize = parseFloat(riskCalculator.farmSize) || 0
  if (farmSize < 1) {
    score += 8 // Small farms are riskier
  } else if (farmSize > 5) {
    score -= 3 // Larger farms have economies of scale
  }

  // Ensure score is within bounds
  riskCalculator.calculatedScore = Math.max(0, Math.min(100, Math.round(score)))
}

const getRiskCategory = (score) => {
  if (score <= 40) return 'Low'
  if (score <= 70) return 'Medium'
  return 'High'
}

const getRiskScoreColor = (score) => {
  if (score <= 40) return 'text-green-600'
  if (score <= 70) return 'text-yellow-600'
  return 'text-red-600'
}

const getRiskRecommendation = (score) => {
  if (score <= 40) {
    return 'Suitable for standard insurance coverage with regular premium rates.'
  } else if (score <= 70) {
    return 'Acceptable risk with possible premium adjustments and enhanced monitoring.'
  } else {
    return 'High-risk application requiring detailed evaluation and special terms.'
  }
}

const exportRiskReport = () => {
  // Implement risk report export functionality
  console.log('Exporting risk assessment report...')
  // You would typically generate a PDF or Excel report here
}

const refreshRiskData = async () => {
  loading.value = true
  try {
    // Simulate API call to refresh risk data
    await new Promise(resolve => setTimeout(resolve, 1500))
    console.log('Risk data refreshed')
  } catch (error) {
    console.error('Failed to refresh risk data:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

// Initialize
onMounted(() => {
  console.log('Risk Factors page loaded')
})
</script>

<style scoped>
/* Custom styles for risk assessment components */
.risk-factor-card {
  transition: all 0.3s ease;
}

.risk-factor-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}

.risk-score-display {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* Animate progress bars */
.risk-progress-bar {
  transition: width 0.8s ease-in-out;
}

/* Custom scrollbar for alerts */
.risk-alerts-container::-webkit-scrollbar {
  width: 4px;
}

.risk-alerts-container::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.risk-alerts-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}
</style>