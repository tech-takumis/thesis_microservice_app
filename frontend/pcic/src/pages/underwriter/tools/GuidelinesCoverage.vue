<template>
  <AuthenticatedLayout
    :navigation="underwriterNavigation"
    role-title="Underwriter Portal"
    page-title="Guidelines Coverage"
  >
    <!-- Header -->
    <template #header>
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-gray-900">Coverage Guidelines</h2>
          <p class="mt-1 text-sm text-gray-600">
            Comprehensive coverage details and benefit calculations for rice crop insurance
          </p>
        </div>
        <div class="flex items-center space-x-3">
          <!-- Coverage Calculator -->
          <button
            @click="showCalculator = true"
            class="inline-flex items-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition"
          >
            <Calculator class="w-4 h-4 mr-2" />
            Coverage Calculator
          </button>
          <!-- Quick Reference -->
          <button
            @click="showQuickRef = true"
            class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
          >
            <BookOpen class="w-4 h-4 mr-2" />
            Quick Reference
          </button>
        </div>
      </div>
    </template>

    <!-- Main Content -->
    <div class="space-y-8">
      
      <!-- Coverage Overview Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="overview in coverageOverview" :key="overview.title" class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div :class="overview.iconBg" class="rounded-lg p-3">
              <component :is="overview.icon" :class="overview.iconColor" class="w-6 h-6" />
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">{{ overview.title }}</p>
              <p class="text-2xl font-bold text-gray-900">{{ overview.value }}</p>
              <p class="text-xs text-gray-500">{{ overview.subtitle }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Coverage Sections -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
        
        <!-- Coverage Types -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-semibold text-gray-900 flex items-center">
              <Shield class="w-5 h-5 mr-2 text-green-600" />
              Types of Coverage Available
            </h3>
          </div>
          <div class="p-6">
            <div class="space-y-6">
              <div v-for="coverage in coverageTypes" :key="coverage.id" class="border rounded-lg p-4">
                <div class="flex items-start justify-between mb-3">
                  <div class="flex items-center">
                    <div :class="coverage.colorClass" class="w-4 h-4 rounded-full mr-3"></div>
                    <h4 class="font-semibold text-gray-800">{{ coverage.name }}</h4>
                  </div>
                  <span class="text-sm font-medium text-green-600">{{ coverage.percentage }}%</span>
                </div>
                <p class="text-sm text-gray-600 mb-3">{{ coverage.description }}</p>
                <div class="space-y-2">
                  <div class="text-sm">
                    <span class="font-medium text-gray-700">Coverage Amount:</span>
                    <span class="text-green-600 font-semibold ml-2">{{ coverage.amount }}</span>
                  </div>
                  <div class="text-xs text-gray-500">
                    <strong>Includes:</strong> {{ coverage.includes.join(', ') }}
                  </div>
                  <div v-if="coverage.conditions" class="text-xs text-gray-500">
                    <strong>Conditions:</strong> {{ coverage.conditions.join(', ') }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Premium Calculation -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-semibold text-gray-900 flex items-center">
              <Banknote class="w-5 h-5 mr-2 text-green-600" />
              Premium Calculation Structure
            </h3>
          </div>
          <div class="p-6">
            <div class="space-y-6">
              <!-- Base Premium -->
              <div class="border-l-4 border-blue-500 pl-4">
                <h4 class="font-semibold text-gray-800 mb-2">Base Premium Rate</h4>
                <div class="grid grid-cols-2 gap-4">
                  <div class="text-center p-3 bg-blue-50 rounded">
                    <div class="text-2xl font-bold text-blue-600">2.5%</div>
                    <div class="text-sm text-blue-700">Standard Rate</div>
                  </div>
                  <div class="text-center p-3 bg-blue-50 rounded">
                    <div class="text-2xl font-bold text-blue-600">85%</div>
                    <div class="text-sm text-blue-700">Cost Coverage</div>
                  </div>
                </div>
              </div>
              
              <!-- Risk Adjustments -->
              <div class="space-y-3">
                <h4 class="font-semibold text-gray-800">Risk-Based Adjustments</h4>
                <div v-for="adjustment in premiumAdjustments" :key="adjustment.factor" class="flex justify-between items-center p-3 border rounded">
                  <div>
                    <span class="text-sm font-medium text-gray-900">{{ adjustment.factor }}</span>
                    <p class="text-xs text-gray-600">{{ adjustment.description }}</p>
                  </div>
                  <span :class="adjustment.impact === 'increase' ? 'text-red-600' : 'text-green-600'" class="text-sm font-semibold">
                    {{ adjustment.impact === 'increase' ? '+' : '-' }}{{ adjustment.percentage }}%
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Coverage Benefits Breakdown -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center">
            <TrendingUp class="w-5 h-5 mr-2 text-green-600" />
            Detailed Benefits Breakdown
          </h3>
        </div>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Benefit Category</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Coverage %</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Max Amount/Ha</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Conditions</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Claim Process</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="benefit in detailedBenefits" :key="benefit.category" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <component :is="benefit.icon" class="w-5 h-5 text-gray-400 mr-3" />
                    <div>
                      <div class="text-sm font-medium text-gray-900">{{ benefit.category }}</div>
                      <div class="text-sm text-gray-500">{{ benefit.description }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" 
                        :class="benefit.coverageClass">{{ benefit.coverage }}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ benefit.maxAmount }}</td>
                <td class="px-6 py-4">
                  <ul class="text-sm text-gray-600 space-y-1">
                    <li v-for="condition in benefit.conditions" :key="condition" class="flex items-start">
                      <div class="w-1.5 h-1.5 bg-gray-400 rounded-full mt-2 mr-2 flex-shrink-0"></div>
                      {{ condition }}
                    </li>
                  </ul>
                </td>
                <td class="px-6 py-4">
                  <div class="text-sm text-gray-600">
                    <div class="font-medium">{{ benefit.claimProcess.timeline }}</div>
                    <div class="text-xs text-gray-500">{{ benefit.claimProcess.requirements }}</div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Regional Coverage Map -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center">
            <MapPin class="w-5 h-5 mr-2 text-green-600" />
            Regional Coverage Variations
          </h3>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div v-for="region in regionalCoverage" :key="region.name" class="border rounded-lg p-4">
              <div class="flex items-center justify-between mb-3">
                <h4 class="font-semibold text-gray-800">{{ region.name }}</h4>
                <span :class="region.statusColor" class="px-3 py-1 text-xs font-medium rounded-full">
                  {{ region.tier }}
                </span>
              </div>
              <div class="space-y-3">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Base Coverage:</span>
                  <span class="font-semibold text-gray-900">{{ region.baseCoverage }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Premium Rate:</span>
                  <span class="font-semibold" :class="region.rateColor">{{ region.premiumRate }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Risk Level:</span>
                  <span class="font-semibold text-gray-900">{{ region.riskLevel }}</span>
                </div>
                <div class="pt-2 border-t">
                  <p class="text-xs text-gray-500">{{ region.notes }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Coverage Calculator Modal -->
    <div v-if="showCalculator" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="closeCalculator">
      <div class="relative top-10 mx-auto p-5 border w-11/12 max-w-2xl shadow-lg rounded-md bg-white" @click.stop>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900">Coverage Calculator</h3>
          <button @click="closeCalculator" class="text-gray-400 hover:text-gray-600">
            <X class="w-6 h-6" />
          </button>
        </div>
        
        <div class="space-y-6">
          <!-- Calculator Inputs -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Farm Size (hectares)</label>
              <input
                v-model.number="calculator.farmSize"
                type="number"
                step="0.1"
                min="0.5"
                max="5"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-green-500 focus:border-green-500"
                placeholder="Enter farm size"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Production Cost per Ha</label>
              <input
                v-model.number="calculator.productionCost"
                type="number"
                min="50000"
                max="100000"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-green-500 focus:border-green-500"
                placeholder="Enter production cost"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Risk Level</label>
              <select 
                v-model="calculator.riskLevel"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-green-500 focus:border-green-500"
              >
                <option value="low">Low Risk (2.5%)</option>
                <option value="medium">Medium Risk (4.0%)</option>
                <option value="high">High Risk (6.5%)</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Coverage Type</label>
              <select 
                v-model="calculator.coverageType"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-green-500 focus:border-green-500"
              >
                <option value="standard">Standard (85%)</option>
                <option value="enhanced">Enhanced (90%)</option>
                <option value="premium">Premium (95%)</option>
              </select>
            </div>
          </div>
          
          <!-- Calculation Results -->
          <div v-if="calculatedResults" class="bg-gray-50 rounded-lg p-6">
            <h4 class="font-semibold text-gray-800 mb-4">Calculation Results</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="text-center">
                <div class="text-2xl font-bold text-green-600">₱{{ calculatedResults.totalCoverage.toLocaleString() }}</div>
                <div class="text-sm text-gray-600">Total Coverage</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-blue-600">₱{{ calculatedResults.annualPremium.toLocaleString() }}</div>
                <div class="text-sm text-gray-600">Annual Premium</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-purple-600">{{ calculatedResults.premiumRate }}%</div>
                <div class="text-sm text-gray-600">Premium Rate</div>
              </div>
            </div>
            <div class="mt-4 text-xs text-gray-500">
              * Calculations are estimates. Final rates may vary based on detailed assessment.
            </div>
          </div>
          
          <div class="flex justify-end space-x-3 pt-4 border-t">
            <button
              @click="closeCalculator"
              class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Close
            </button>
            <button
              @click="calculateCoverage"
              class="px-4 py-2 bg-green-600 text-white rounded-md text-sm font-medium hover:bg-green-700"
            >
              Calculate
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
  Calculator,
  BookOpen,
  Shield,
  Banknote,
  TrendingUp,
  MapPin,
  X,
  Sprout,
  Cloud,
  Droplets,
  Bug,
  Zap,
  Wind,
  Sun
} from 'lucide-vue-next'

// Navigation
const underwriterNavigation = UNDERWRITER_NAVIGATION

// Modal states
const showCalculator = ref(false)
const showQuickRef = ref(false)

// Calculator data
const calculator = ref({
  farmSize: 1,
  productionCost: 75000,
  riskLevel: 'low',
  coverageType: 'standard'
})

const calculatedResults = ref(null)

// Coverage overview stats
const coverageOverview = ref([
  {
    title: 'Max Coverage',
    value: '₱75,000',
    subtitle: 'per hectare',
    icon: Shield,
    iconBg: 'bg-green-100',
    iconColor: 'text-green-600'
  },
  {
    title: 'Base Premium',
    value: '2.5%',
    subtitle: 'of insured amount',
    icon: Banknote,
    iconBg: 'bg-blue-100',
    iconColor: 'text-blue-600'
  },
  {
    title: 'Coverage Ratio',
    value: '85%',
    subtitle: 'of production cost',
    icon: TrendingUp,
    iconBg: 'bg-purple-100',
    iconColor: 'text-purple-600'
  },
  {
    title: 'Claim Period',
    value: '30',
    subtitle: 'days processing',
    icon: Calculator,
    iconBg: 'bg-yellow-100',
    iconColor: 'text-yellow-600'
  }
])

// Coverage types
const coverageTypes = ref([
  {
    id: 1,
    name: 'Standard Coverage',
    percentage: 85,
    amount: '₱63,750 per hectare',
    description: 'Basic production cost coverage for standard farming practices',
    colorClass: 'bg-green-500',
    includes: ['Seeds', 'Fertilizers', 'Labor costs', 'Basic equipment'],
    conditions: ['Minimum 0.5 hectares', 'Approved varieties only', 'Standard farming practices']
  },
  {
    id: 2,
    name: 'Enhanced Coverage',
    percentage: 90,
    amount: '₱67,500 per hectare',
    description: 'Extended coverage including additional input costs and equipment',
    colorClass: 'bg-blue-500',
    includes: ['All standard items', 'Pesticides', 'Irrigation costs', 'Equipment rental'],
    conditions: ['Minimum 1 hectare', 'Certified organic practices', 'Technology adoption']
  },
  {
    id: 3,
    name: 'Premium Coverage',
    percentage: 95,
    amount: '₱71,250 per hectare',
    description: 'Comprehensive coverage with maximum protection and benefits',
    colorClass: 'bg-purple-500',
    includes: ['All enhanced items', 'Post-harvest losses', 'Quality premiums', 'Market support'],
    conditions: ['Minimum 2 hectares', 'Contract farming', 'Premium variety cultivation']
  }
])

// Premium adjustments
const premiumAdjustments = ref([
  {
    factor: 'Geographic Risk',
    description: 'High typhoon/flood prone areas',
    percentage: 1.5,
    impact: 'increase'
  },
  {
    factor: 'Irrigation Access',
    description: 'Fully irrigated farms',
    percentage: 0.5,
    impact: 'decrease'
  },
  {
    factor: 'Farm Size',
    description: 'Farms larger than 3 hectares',
    percentage: 0.3,
    impact: 'decrease'
  },
  {
    factor: 'Claims History',
    description: 'No claims in last 3 years',
    percentage: 0.8,
    impact: 'decrease'
  },
  {
    factor: 'Late Planting',
    description: 'Planting outside recommended season',
    percentage: 2.0,
    impact: 'increase'
  }
])

// Detailed benefits
const detailedBenefits = ref([
  {
    category: 'Natural Disasters',
    description: 'Typhoons, floods, droughts',
    coverage: '100%',
    coverageClass: 'bg-green-100 text-green-800',
    maxAmount: '₱75,000',
    icon: Wind,
    conditions: [
      'PAGASA weather declaration required',
      'Minimum 30% crop loss',
      '72-hour notification required'
    ],
    claimProcess: {
      timeline: '14-21 days',
      requirements: 'Weather reports, field inspection'
    }
  },
  {
    category: 'Pest and Disease',
    description: 'Insect damage, fungal diseases',
    coverage: '85%',
    coverageClass: 'bg-yellow-100 text-yellow-800',
    maxAmount: '₱63,750',
    icon: Bug,
    conditions: [
      'Proper pest management followed',
      'Minimum 25% crop damage',
      'Expert assessment required'
    ],
    claimProcess: {
      timeline: '21-30 days',
      requirements: 'Entomologist report, treatment records'
    }
  },
  {
    category: 'Planting Failure',
    description: 'Failed germination, replanting',
    coverage: '70%',
    coverageClass: 'bg-orange-100 text-orange-800',
    maxAmount: '₱52,500',
    icon: Sprout,
    conditions: [
      'Certified seed varieties only',
      'Proper planting methods',
      'Within 30 days of planting'
    ],
    claimProcess: {
      timeline: '7-14 days',
      requirements: 'Seed certificates, planting records'
    }
  },
  {
    category: 'Irrigation Failure',
    description: 'Water system breakdown',
    coverage: '60%',
    coverageClass: 'bg-blue-100 text-blue-800',
    maxAmount: '₱45,000',
    icon: Droplets,
    conditions: [
      'Registered irrigation system',
      'System maintenance records',
      'Alternative water source unavailable'
    ],
    claimProcess: {
      timeline: '14-21 days',
      requirements: 'System inspection, maintenance logs'
    }
  }
])

// Regional coverage
const regionalCoverage = ref([
  {
    name: 'Central Luzon',
    tier: 'Tier 1',
    statusColor: 'bg-green-100 text-green-800',
    baseCoverage: '₱75,000/ha',
    premiumRate: '2.5%',
    rateColor: 'text-green-600',
    riskLevel: 'Low',
    notes: 'Primary rice production area with excellent infrastructure and support services'
  },
  {
    name: 'Cagayan Valley',
    tier: 'Tier 1',
    statusColor: 'bg-green-100 text-green-800',
    baseCoverage: '₱75,000/ha',
    premiumRate: '3.0%',
    rateColor: 'text-yellow-600',
    riskLevel: 'Low-Medium',
    notes: 'High production area with seasonal flood risks requiring enhanced monitoring'
  },
  {
    name: 'Central Visayas',
    tier: 'Tier 2',
    statusColor: 'bg-yellow-100 text-yellow-800',
    baseCoverage: '₱70,000/ha',
    premiumRate: '4.0%',
    rateColor: 'text-orange-600',
    riskLevel: 'Medium',
    notes: 'Moderate production with typhoon exposure requiring additional risk assessment'
  },
  {
    name: 'Northern Mindanao',
    tier: 'Tier 2',
    statusColor: 'bg-yellow-100 text-yellow-800',
    baseCoverage: '₱65,000/ha',
    premiumRate: '4.5%',
    rateColor: 'text-red-600',
    riskLevel: 'Medium-High',
    notes: 'Emerging production area with limited infrastructure and higher weather risks'
  },
  {
    name: 'Eastern Visayas',
    tier: 'Tier 3',
    statusColor: 'bg-red-100 text-red-800',
    baseCoverage: '₱60,000/ha',
    premiumRate: '6.0%',
    rateColor: 'text-red-600',
    riskLevel: 'High',
    notes: 'High-risk typhoon corridor with selective coverage and enhanced requirements'
  },
  {
    name: 'ARMM Provinces',
    tier: 'Tier 3',
    statusColor: 'bg-red-100 text-red-800',
    baseCoverage: '₱55,000/ha',
    premiumRate: '6.5%',
    rateColor: 'text-red-600',
    riskLevel: 'High',
    notes: 'Special administrative region with limited coverage and strict eligibility criteria'
  }
])

// Methods
const closeCalculator = () => {
  showCalculator.value = false
  calculatedResults.value = null
}

const calculateCoverage = () => {
  const { farmSize, productionCost, riskLevel, coverageType } = calculator.value
  
  // Base coverage percentages
  const coverageRates = {
    standard: 0.85,
    enhanced: 0.90,
    premium: 0.95
  }
  
  // Premium rates by risk level
  const premiumRates = {
    low: 2.5,
    medium: 4.0,
    high: 6.5
  }
  
  const coverageRate = coverageRates[coverageType]
  const premiumRate = premiumRates[riskLevel]
  
  const totalProductionCost = farmSize * productionCost
  const totalCoverage = totalProductionCost * coverageRate
  const annualPremium = totalCoverage * (premiumRate / 100)
  
  calculatedResults.value = {
    totalCoverage: Math.round(totalCoverage),
    annualPremium: Math.round(annualPremium),
    premiumRate: premiumRate
  }
}
</script>

<style scoped>
/* Custom table styling */
.table-hover tbody tr:hover {
  background-color: #f9fafb;
}

/* Modal animation */
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.3s;
}
.modal-enter, .modal-leave-to {
  opacity: 0;
}

/* Custom badge colors */
.coverage-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}
</style>
