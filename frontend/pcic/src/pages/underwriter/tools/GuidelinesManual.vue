<template>
  <AuthenticatedLayout
    :navigation="underwriterNavigation"
    role-title="Underwriter Portal"
    page-title="Guidelines Manual"
  >
    <!-- Header -->
    <template #header>
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-semibold text-green-600">Underwriting Guidelines Manual</h2>
          <p class="mt-1 text-sm text-gray-600">
            Comprehensive manual for rice crop insurance underwriting procedures and guidelines
          </p>
        </div>
        <div class="flex items-center space-x-3">
          <!-- Search -->
          <div class="relative">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search guidelines..."
              class="pl-9 pr-4 py-2 w-64 border border-gray-300 rounded-md text-sm focus:ring-green-500 focus:border-green-500"
            />
          </div>
         
        </div>
      </div>
    </template>

    <!-- Main Content -->
    <div class="h-full flex">
      <!-- Table of Contents Sidebar -->
      <div class="w-80 bg-gray-100 border border-gray-300 rounded-lg mr-6 overflow-hidden">
        <div class="px-6 py-4 bg-gray-100 border-b border-gray-300">
          <h3 class="text-lg font-semibold text-gray-900">Table of Contents</h3>
        </div>
        <div class="p-4 overflow-y-auto h-full">
          <nav class="space-y-2">
            <a
              v-for="section in filteredSections"
              :key="section.id"
              :href="`#${section.id}`"
              @click="scrollToSection(section.id)"
              class="block px-3 py-2 text-sm rounded-md hover:bg-gray-100 transition-colors"
              :class="activeSection === section.id ? 'bg-green-100 text-green-800 font-medium' : 'text-gray-700'"
            >
              {{ section.title }}
            </a>
          </nav>
        </div>
      </div>

      <!-- Guidelines Content -->
      <div class="flex-1 bg-gray-100 rounded-xl border border-gray-300 overflow-hidden">
        <div class="h-full overflow-y-auto p-8" ref="contentArea">
          
          <!-- Section 1: Overview -->
          <section id="overview" class="mb-12">
            <h2 class="text-3xl font-bold text-gray-800 mb-6">1. Overview</h2>
            <div class="prose max-w-none">
              <p class="text-lg text-gray-700 mb-4">
                This manual provides comprehensive guidelines for underwriting rice crop insurance policies 
                in the Philippines. It covers procedures, risk assessment, coverage determination, and 
                documentation requirements.
              </p>
              <div class="bg-green-100 border-l-4 border-green-600 p-6 my-6">
                <div class="flex">
                  <Info class="h-6 w-6 text-green-600 mr-3 flex-shrink-0 mt-1" />
                  <div>
                    <h4 class="text-lg font-semibold text-green-600 mb-2">Important Note</h4>
                    <p class="text-gray-800">
                      All underwriting decisions must comply with the Philippine Crop Insurance Corporation (PCIC) 
                      regulations and this manual's guidelines. Regular updates are issued quarterly.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Section 2: Application Process -->
          <section id="application-process" class="mb-12">
            <h2 class="text-3xl font-bold text-gray-800 mb-6">2. Application Process</h2>
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div class="space-y-6">
                <h3 class="text-xl font-semibold text-gray-800">2.1 Required Documents</h3>
                <ul class="space-y-3">
                  <li v-for="doc in requiredDocuments" :key="doc.id" class="flex items-start">
                    <CheckCircle class="h-5 w-5 text-green-500 mr-3 mt-0.5 flex-shrink-0" />
                    <div>
                      <span class="font-medium text-gray-900">{{ doc.name }}</span>
                      <p class="text-sm text-gray-600">{{ doc.description }}</p>
                    </div>
                  </li>
                </ul>
              </div>
              <div class="space-y-6">
                <h3 class="text-xl font-semibold text-gray-800">2.2 Processing Timeline</h3>
                <div class="space-y-4">
                  <div v-for="step in processingSteps" :key="step.id" class="flex">
                    <div class="flex-shrink-0">
                      <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                        <span class="text-sm font-medium text-green-800">{{ step.day }}</span>
                      </div>
                    </div>
                    <div class="ml-4">
                      <h4 class="text-sm font-medium text-gray-900">{{ step.title }}</h4>
                      <p class="text-sm text-gray-600">{{ step.description }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Section 3: Risk Assessment -->
          <section id="risk-assessment" class="mb-12">
            <h2 class="text-3xl font-bold text-gray-800 mb-6">3. Risk Assessment Criteria</h2>
            <div class="space-y-8">
              <div class="overflow-hidden bg-white border border-gray-200 rounded-lg">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Risk Factor</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Low Risk</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Medium Risk</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">High Risk</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="criteria in riskCriteria" :key="criteria.factor">
                      <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ criteria.factor }}</td>
                      <td class="px-6 py-4 text-sm text-gray-600">{{ criteria.low }}</td>
                      <td class="px-6 py-4 text-sm text-gray-600">{{ criteria.medium }}</td>
                      <td class="px-6 py-4 text-sm text-gray-600">{{ criteria.high }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </section>

          <!-- Section 4: Premium Calculation -->
          <section id="premium-calculation" class="mb-12">
            <h2 class="text-3xl font-bold text-gray-800 mb-6">4. Premium Calculation Guidelines</h2>
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div v-for="tier in premiumTiers" :key="tier.name" class="border rounded-lg p-6">
                <div class="flex items-center mb-4">
                  <div :class="tier.colorClass" class="w-4 h-4 rounded-full mr-3"></div>
                  <h3 class="text-lg font-semibold">{{ tier.name }}</h3>
                </div>
                <div class="space-y-3">
                  <p class="text-sm text-gray-600">{{ tier.description }}</p>
                  <div class="text-2xl font-bold" :class="tier.textClass">{{ tier.rate }}%</div>
                  <ul class="text-sm space-y-1">
                    <li v-for="criteria in tier.criteria" :key="criteria" class="text-gray-600">
                      • {{ criteria }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </section>

          <!-- Section 5: Coverage Limits -->
          <section id="coverage-limits" class="mb-12">
            <h2 class="text-3xl font-bold text-gray-800 mb-6">5. Coverage Limits and Conditions</h2>
            <div class="space-y-6">
              <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-6">
                <h3 class="text-lg font-semibold text-yellow-800 mb-3">Maximum Coverage Amounts</h3>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div class="text-center">
                    <div class="text-2xl font-bold text-yellow-800">₱75,000</div>
                    <div class="text-sm text-yellow-700">Per Hectare</div>
                  </div>
                  <div class="text-center">
                    <div class="text-2xl font-bold text-yellow-800">₱375,000</div>
                    <div class="text-sm text-yellow-700">Per Farmer (5 ha max)</div>
                  </div>
                  <div class="text-center">
                    <div class="text-2xl font-bold text-yellow-800">85%</div>
                    <div class="text-sm text-yellow-700">Production Cost Coverage</div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Section 6: Claims Process -->
          <section id="claims-process" class="mb-12">
            <h2 class="text-3xl font-bold text-gray-800 mb-6">6. Claims Processing Guidelines</h2>
            <div class="space-y-6">
              <div class="bg-white rounded-xl p-6">
                <h3 class="text-lg font-semibold text-gray-800 mb-4">Claim Notification Timeline</h3>
                <div class="space-y-4">
                  <div v-for="timeline in claimsTimeline" :key="timeline.stage" class="flex items-center">
                    <div class="w-24 text-sm font-medium text-gray-600">{{ timeline.timeframe }}</div>
                    <div class="flex-1 ml-4">
                      <div class="font-medium text-gray-900">{{ timeline.stage }}</div>
                      <div class="text-sm text-gray-600">{{ timeline.description }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'
import { 
  Search,
  Printer,
  Info,
  CheckCircle
} from 'lucide-vue-next'

// Navigation
const underwriterNavigation = UNDERWRITER_NAVIGATION

// Search
const searchQuery = ref('')
const activeSection = ref('overview')
const contentArea = ref(null)

// Guidelines data
const sections = ref([
  { id: 'overview', title: '1. Overview' },
  { id: 'application-process', title: '2. Application Process' },
  { id: 'risk-assessment', title: '3. Risk Assessment' },
  { id: 'premium-calculation', title: '4. Premium Calculation' },
  { id: 'coverage-limits', title: '5. Coverage Limits' },
  { id: 'claims-process', title: '6. Claims Process' }
])

const requiredDocuments = ref([
  {
    id: 1,
    name: 'Farmer Registration Form',
    description: 'Complete registration with farmer identification and contact details'
  },
  {
    id: 2,
    name: 'Land Title or Lease Agreement',
    description: 'Proof of land ownership or authorized use for cultivation'
  },
  {
    id: 3,
    name: 'Planting Schedule',
    description: 'Detailed planting dates and expected harvest timeline'
  },
  {
    id: 4,
    name: 'Previous Yield Records',
    description: 'Historical production data for the last 3 seasons (if available)'
  },
  {
    id: 5,
    name: 'Farm Location Map',
    description: 'GPS coordinates and detailed location of insured parcels'
  }
])

const processingSteps = ref([
  {
    id: 1,
    day: '1',
    title: 'Application Submission',
    description: 'Initial review of submitted documents and forms'
  },
  {
    id: 2,
    day: '3',
    title: 'Field Inspection',
    description: 'On-site verification of crop conditions and farm location'
  },
  {
    id: 3,
    day: '5',
    title: 'Risk Assessment',
    description: 'Evaluation of risk factors and premium calculation'
  },
  {
    id: 4,
    day: '7',
    title: 'Policy Issuance',
    description: 'Final approval and policy document generation'
  }
])

const riskCriteria = ref([
  {
    factor: 'Location Risk',
    low: 'Low typhoon/flood zone',
    medium: 'Moderate weather exposure',
    high: 'High disaster-prone area'
  },
  {
    factor: 'Farm Size',
    low: '> 3 hectares',
    medium: '1-3 hectares',
    high: '< 1 hectare'
  },
  {
    factor: 'Planting Method',
    low: 'Transplanting',
    medium: 'Direct seeding (wet)',
    high: 'Direct seeding (dry)'
  },
  {
    factor: 'Irrigation',
    low: 'Fully irrigated',
    medium: 'Partially irrigated',
    high: 'Rainfed only'
  },
  {
    factor: 'Farmer Experience',
    low: '> 10 years',
    medium: '5-10 years',
    high: '< 5 years'
  }
])

const premiumTiers = ref([
  {
    name: 'Standard Rate',
    rate: '2.5',
    description: 'Default premium rate for standard risk applications',
    colorClass: 'bg-green-500',
    textClass: 'text-green-600',
    criteria: [
      'Low to medium risk factors',
      'Standard coverage amount',
      'Experienced farmers',
      'Good irrigation access'
    ]
  },
  {
    name: 'Adjusted Rate',
    rate: '4.0',
    description: 'Increased premium for moderate risk applications',
    colorClass: 'bg-yellow-500',
    textClass: 'text-yellow-600',
    criteria: [
      'Medium to high risk factors',
      'Higher coverage amounts',
      'Moderate weather exposure',
      'Limited irrigation'
    ]
  },
  {
    name: 'High Risk Rate',
    rate: '6.5',
    description: 'Maximum premium for high-risk applications',
    colorClass: 'bg-red-500',
    textClass: 'text-red-600',
    criteria: [
      'Multiple high risk factors',
      'Maximum coverage amounts',
      'High disaster exposure',
      'Rainfed cultivation'
    ]
  }
])

const claimsTimeline = ref([
  {
    timeframe: 'Within 72h',
    stage: 'Loss Notification',
    description: 'Farmer must report crop damage immediately after occurrence'
  },
  {
    timeframe: 'Within 7d',
    stage: 'Formal Claim Filing',
    description: 'Submit complete claim forms with supporting documentation'
  },
  {
    timeframe: 'Within 14d',
    stage: 'Field Assessment',
    description: 'PCIC adjustor conducts on-site damage evaluation'
  },
  {
    timeframe: 'Within 30d',
    stage: 'Claim Settlement',
    description: 'Processing and payment of approved claims'
  }
])

// Computed
const filteredSections = computed(() => {
  if (!searchQuery.value) return sections.value
  
  const query = searchQuery.value.toLowerCase()
  return sections.value.filter(section => 
    section.title.toLowerCase().includes(query)
  )
})

// Methods
const scrollToSection = (sectionId) => {
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
    activeSection.value = sectionId
  }
}

const printGuidelines = () => {
  window.print()
}

// Lifecycle
onMounted(() => {
  // Setup scroll spy for active section highlighting
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          activeSection.value = entry.target.id
        }
      })
    },
    { threshold: 0.3 }
  )
  
  nextTick(() => {
    sections.value.forEach((section) => {
      const element = document.getElementById(section.id)
      if (element) {
        observer.observe(element)
      }
    })
  })
})
</script>

<style scoped>
/* Print styles */
@media print {
  .no-print {
    display: none !important;
  }
}

/* Smooth scrolling for anchor links */
html {
  scroll-behavior: smooth;
}

/* Custom prose styles */
.prose {
  color: #374151;
  line-height: 1.7;
}

.prose p {
  margin-bottom: 1rem;
}

.prose h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}
</style>