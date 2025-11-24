<template>
    <AuthenticatedLayout :navigation="navigation">
        <!-- Header with Search and Filter -->
        <div class="mb-6">
            <div class="flex items-center justify-between mb-4">
                <h1 class="text-2xl font-bold text-gray-900">RSBSA Management</h1>

                <div class="flex items-center space-x-3">
                    <!-- Search Bar -->
                    <div class="relative">
                        <input
                            type="text"
                            v-model="searchQuery"
                            placeholder="Search RSBSA ID, Farmer Name..."
                            class="w-80 pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                        />
                        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    </div>

                    <!-- Filter Button -->
                    <button
                        @click="showFilters = !showFilters"
                        class="flex items-center px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                        :class="{ 'bg-green-50 border-green-500': showFilters }"
                    >
                        <Filter class="h-5 w-5 mr-2" :class="showFilters ? 'text-green-600' : 'text-gray-600'" />
                        <span :class="showFilters ? 'text-green-600' : 'text-gray-700'">Filters</span>
                        <ChevronDown
                            class="h-4 w-4 ml-2 transition-transform"
                            :class="[
                                showFilters ? 'rotate-180 text-green-600' : 'text-gray-600'
                            ]"
                        />
                    </button>

                    <!-- Create RSBSA Button -->
                    <button
                        @click="showCreateModal = true"
                        class="flex items-center px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors"
                    >
                        <Plus class="h-5 w-5 mr-2" />
                        New RSBSA
                    </button>
                </div>
            </div>

            <!-- Filter Panel -->
            <transition
                enter-active-class="transition ease-out duration-200"
                enter-from-class="opacity-0 -translate-y-2"
                enter-to-class="opacity-100 translate-y-0"
                leave-active-class="transition ease-in duration-150"
                leave-from-class="opacity-100 translate-y-0"
                leave-to-class="opacity-0 -translate-y-2"
            >
                <div v-if="showFilters" class="bg-white border border-gray-200 rounded-lg p-4 mb-4">
                    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                        <!-- Status Filter -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-2">Status</label>
                            <select
                                v-model="filters.status"
                                class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                            >
                                <option value="">All Status</option>
                                <option value="ACTIVE">Active</option>
                                <option value="INACTIVE">Inactive</option>
                                <option value="PENDING">Pending</option>
                            </select>
                        </div>

                        <!-- Registration Year Filter -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-2">Registration Year</label>
                            <select
                                v-model="filters.year"
                                class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                            >
                                <option value="">All Years</option>
                                <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
                            </select>
                        </div>

                        <!-- Municipality Filter -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-2">Municipality</label>
                            <select
                                v-model="filters.municipality"
                                class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                            >
                                <option value="">All Municipalities</option>
                                <!-- Add municipalities dynamically from data -->
                            </select>
                        </div>

                        <!-- Clear Filters Button -->
                        <div class="flex items-end">
                            <button
                                @click="clearFilters"
                                class="w-full px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors"
                            >
                                Clear Filters
                            </button>
                        </div>
                    </div>
                </div>
            </transition>
        </div>

        <!-- Table -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            <!-- Table Header with Bulk Actions -->
            <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
                <div class="flex items-center space-x-4">
                    <h2 class="text-lg font-semibold text-gray-900">
                        RSBSA Records
                    </h2>

                    <!-- Bulk Delete Button -->
                    <transition
                        enter-active-class="transition ease-out duration-200"
                        enter-from-class="opacity-0 scale-95"
                        enter-to-class="opacity-100 scale-100"
                        leave-active-class="transition ease-in duration-150"
                        leave-from-class="opacity-100 scale-100"
                        leave-to-class="opacity-0 scale-95"
                    >
                        <button
                            v-if="selectedRsbsa.length > 0"
                            @click="confirmBulkDelete"
                            class="flex items-center px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors"
                        >
                            <Trash2 class="h-4 w-4 mr-2" />
                            Delete Selected ({{ selectedRsbsa.length }})
                        </button>
                    </transition>
                </div>

                <!-- Select All Checkbox -->
                <div class="flex items-center space-x-2">
                    <input
                        type="checkbox"
                        :checked="isAllSelected"
                        @change="toggleSelectAll"
                        class="h-4 w-4 text-green-600 border-gray-300 rounded focus:ring-green-500"
                    />
                    <label class="text-sm text-gray-700">Select All</label>
                </div>
            </div>

            <!-- Table Content -->
            <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="w-12 px-6 py-3">
                                <span class="sr-only">Checkbox</span>
                            </th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                RSBSA ID
                            </th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Farmer Name
                            </th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Contact
                            </th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Location
                            </th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Primary Crop
                            </th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Farm Area
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <tr
                            v-for="rsbsa in paginatedRsbsa"
                            :key="rsbsa.rsbsaId"
                            class="hover:bg-gray-50 transition-colors cursor-pointer"
                        >
                            <!-- Checkbox -->
                            <td class="px-6 py-4 whitespace-nowrap">
                                <input
                                    type="checkbox"
                                    :checked="selectedRsbsa.includes(rsbsa.rsbsaId)"
                                    @change="toggleSelect(rsbsa.rsbsaId)"
                                    @click.stop
                                    class="h-4 w-4 text-green-600 border-gray-300 rounded focus:ring-green-500"
                                />
                            </td>

                            <!-- RSBSA ID -->
                            <td
                                class="px-6 py-4 whitespace-nowrap text-sm font-medium text-green-600"
                                @click="navigateToDetail(rsbsa.rsbsaId)"
                            >
                                {{ rsbsa.rsbsaId }}
                            </td>

                            <!-- Farmer Name -->
                            <td
                                class="px-6 py-4 whitespace-nowrap"
                                @click="navigateToDetail(rsbsa.rsbsaId)"
                            >
                                <div class="text-sm font-medium text-gray-900">
                                    {{ formatFullName(rsbsa.firstName, rsbsa.middleName, rsbsa.lastName) }}
                                </div>
                            </td>

                            <!-- Contact -->
                            <td
                                class="px-6 py-4 whitespace-nowrap"
                                @click="navigateToDetail(rsbsa.rsbsaId)"
                            >
                                <div class="text-sm text-gray-900">{{ rsbsa.contactNumber || 'N/A' }}</div>
                            </td>

                            <!-- Location -->
                            <td
                                class="px-6 py-4"
                                @click="navigateToDetail(rsbsa.rsbsaId)"
                            >
                                <div class="text-sm text-gray-900">
                                    {{ formatLocation(rsbsa.barangay, rsbsa.municipality) }}
                                </div>
                            </td>

                            <!-- Primary Crop -->
                            <td
                                class="px-6 py-4 whitespace-nowrap"
                                @click="navigateToDetail(rsbsa.rsbsaId)"
                            >
                                <div class="text-sm text-gray-900">
                                    {{ rsbsa.primaryCrop || 'N/A' }}
                                </div>
                            </td>

                            <!-- Farm Area -->
                            <td
                                class="px-6 py-4 whitespace-nowrap"
                                @click="navigateToDetail(rsbsa.rsbsaId)"
                            >
                                <div class="text-sm text-gray-900">
                                    {{ rsbsa.farmArea ? `${rsbsa.farmArea} ha` : 'N/A' }}
                                </div>
                            </td>
                        </tr>

                        <!-- Empty State -->
                        <tr v-if="filteredRsbsa.length === 0">
                            <td colspan="7" class="px-6 py-12 text-center">
                                <div class="flex flex-col items-center">
                                    <Database class="h-12 w-12 text-gray-300 mb-3" />
                                    <p class="text-gray-500 text-base font-medium">No RSBSA records found</p>
                                    <p class="text-gray-400 text-sm mt-1">
                                        {{ searchQuery ? 'Try adjusting your search or filters' : 'Get started by adding a new record' }}
                                    </p>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Pagination -->
            <div v-if="filteredRsbsa.length > 0" class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
                <div class="flex items-center space-x-2">
                    <span class="text-sm text-gray-700">Show</span>
                    <select
                        v-model="perPage"
                        class="border border-gray-300 rounded px-2 py-1 text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    >
                        <option :value="10">10</option>
                        <option :value="25">25</option>
                        <option :value="50">50</option>
                        <option :value="100">100</option>
                    </select>
                    <span class="text-sm text-gray-700">entries</span>
                </div>

                <div class="flex items-center space-x-2">
                    <span class="text-sm text-gray-700">
                        Showing {{ ((currentPage - 1) * perPage) + 1 }} to {{ Math.min(currentPage * perPage, filteredRsbsa.length) }} of {{ filteredRsbsa.length }} records
                    </span>
                </div>

                <div class="flex items-center space-x-2">
                    <button
                        @click="currentPage--"
                        :disabled="currentPage === 1"
                        class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        <ChevronLeft class="h-4 w-4" />
                    </button>

                    <div class="flex space-x-1">
                        <button
                            v-for="page in visiblePages"
                            :key="page"
                            @click="currentPage = page"
                            :class="[
                                'px-3 py-1 border rounded transition-colors',
                                currentPage === page
                                    ? 'bg-green-600 text-white border-green-600'
                                    : 'border-gray-300 hover:bg-gray-50'
                            ]"
                        >
                            {{ page }}
                        </button>
                    </div>

                    <button
                        @click="currentPage++"
                        :disabled="currentPage === totalPages"
                        class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        <ChevronRight class="h-4 w-4" />
                    </button>
                </div>
            </div>
        </div>

        <!-- Create RSBSA Modal (Right Side Slide-in) -->
        <transition
            enter-active-class="transition ease-out duration-300"
            enter-from-class="opacity-0"
            enter-to-class="opacity-100"
            leave-active-class="transition ease-in duration-200"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
        >
            <div
                v-if="showCreateModal"
                class="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-end"
                @click="closeCreateModal"
            >
                <transition
                    enter-active-class="transition ease-out duration-300"
                    enter-from-class="translate-x-full"
                    enter-to-class="translate-x-0"
                    leave-active-class="transition ease-in duration-200"
                    leave-from-class="translate-x-0"
                    leave-to-class="translate-x-full"
                >
                    <div
                        v-if="showCreateModal"
                        class="bg-white w-full max-w-4xl h-full overflow-y-auto"
                        @click.stop
                    >
                        <!-- Modal Header -->
                        <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 z-10">
                            <div class="flex items-center justify-between">
                                <div>
                                    <h2 class="text-xl font-semibold text-gray-900">Create New RSBSA Record</h2>
                                    <p class="text-sm text-gray-600 mt-1">Fill in the farmer's information below</p>
                                </div>
                                <button
                                    @click="closeCreateModal"
                                    class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                                >
                                    <X class="h-5 w-5 text-gray-600" />
                                </button>
                            </div>
                        </div>

                        <!-- Modal Content -->
                        <form @submit.prevent="createRsbsa" class="px-6 py-6">
                            <!-- Personal Information Section -->
                            <div class="mb-8">
                                <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
                                    <User class="h-5 w-5 text-green-600 mr-2" />
                                    Personal Information
                                </h3>
                                <div class="grid grid-cols-2 gap-4">
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            RSBSA ID <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.rsbsaId"
                                            type="text"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Enter RSBSA ID"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            First Name <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.firstName"
                                            type="text"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="First name"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Middle Name
                                        </label>
                                        <input
                                            v-model="formData.middleName"
                                            type="text"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Middle name"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Last Name <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.lastName"
                                            type="text"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Last name"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Gender <span class="text-red-500">*</span>
                                        </label>
                                        <select
                                            v-model="formData.gender"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                        >
                                            <option value="">Select Gender</option>
                                            <option value="MALE">Male</option>
                                            <option value="FEMALE">Female</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Date of Birth <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.dateOfBirth"
                                            type="date"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Civil Status <span class="text-red-500">*</span>
                                        </label>
                                        <select
                                            v-model="formData.civilStatus"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                        >
                                            <option value="">Select Status</option>
                                            <option value="SINGLE">Single</option>
                                            <option value="MARRIED">Married</option>
                                            <option value="WIDOWED">Widowed</option>
                                            <option value="SEPARATED">Separated</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Education Level
                                        </label>
                                        <select
                                            v-model="formData.educationLevel"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                        >
                                            <option value="">Select Level</option>
                                            <option value="ELEMENTARY">Elementary</option>
                                            <option value="HIGH_SCHOOL">High School</option>
                                            <option value="COLLEGE">College</option>
                                            <option value="VOCATIONAL">Vocational</option>
                                            <option value="NONE">None</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Household Size
                                        </label>
                                        <input
                                            v-model.number="formData.householdSize"
                                            type="number"
                                            min="1"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Number of members"
                                        />
                                    </div>

                                    <div class="flex items-center pt-6">
                                        <input
                                            v-model="formData.withDisability"
                                            type="checkbox"
                                            class="h-4 w-4 text-green-600 border-gray-300 rounded focus:ring-green-500"
                                        />
                                        <label class="ml-2 text-sm text-gray-700">
                                            Person with Disability (PWD)
                                        </label>
                                    </div>
                                </div>
                            </div>

                            <!-- Contact Information Section -->
                            <div class="mb-8">
                                <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
                                    <Phone class="h-5 w-5 text-green-600 mr-2" />
                                    Contact Information
                                </h3>
                                <div class="grid grid-cols-2 gap-4">
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Contact Number <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.contactNumber"
                                            type="tel"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="09XX XXX XXXX"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Email Address
                                        </label>
                                        <input
                                            v-model="formData.email"
                                            type="email"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="email@example.com"
                                        />
                                    </div>
                                </div>
                            </div>

                            <!-- Address Information Section -->
                            <div class="mb-8">
                                <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
                                    <MapPin class="h-5 w-5 text-green-600 mr-2" />
                                    Address Information
                                </h3>
                                <div class="grid grid-cols-2 gap-4">
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Province <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.province"
                                            type="text"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Province"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Municipality <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.municipality"
                                            type="text"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Municipality"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Barangay <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.barangay"
                                            type="text"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Barangay"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Street/Purok/Sitio
                                        </label>
                                        <input
                                            v-model="formData.address"
                                            type="text"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Street address"
                                        />
                                    </div>
                                </div>
                            </div>

                            <!-- Farm Information Section -->
                            <div class="mb-8">
                                <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
                                    <Sprout class="h-5 w-5 text-green-600 mr-2" />
                                    Farm Information
                                </h3>
                                <div class="grid grid-cols-2 gap-4">
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Farming Type <span class="text-red-500">*</span>
                                        </label>
                                        <select
                                            v-model="formData.farmingType"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                        >
                                            <option value="">Select Type</option>
                                            <option value="ORGANIC">Organic</option>
                                            <option value="CONVENTIONAL">Conventional</option>
                                            <option value="MIXED">Mixed</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Primary Crop <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model="formData.primaryCrop"
                                            type="text"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="e.g., Rice, Corn"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Secondary Crop
                                        </label>
                                        <input
                                            v-model="formData.secondaryCrop"
                                            type="text"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="e.g., Vegetables"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Farm Area (hectares) <span class="text-red-500">*</span>
                                        </label>
                                        <input
                                            v-model.number="formData.farmArea"
                                            type="number"
                                            step="0.01"
                                            min="0"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="0.00"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Farm Location
                                        </label>
                                        <input
                                            v-model="formData.farmLocation"
                                            type="text"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="Farm location"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Tenure Status <span class="text-red-500">*</span>
                                        </label>
                                        <select
                                            v-model="formData.tenureStatus"
                                            required
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                        >
                                            <option value="">Select Status</option>
                                            <option value="OWNER">Owner</option>
                                            <option value="TENANT">Tenant</option>
                                            <option value="LEASEHOLDER">Leaseholder</option>
                                            <option value="OTHERS">Others</option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <!-- Income Information Section -->
                            <div class="mb-8">
                                <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
                                    <Wallet class="h-5 w-5 text-green-600 mr-2" />
                                    Income Information
                                </h3>
                                <div class="grid grid-cols-2 gap-4">
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Source of Income
                                        </label>
                                        <input
                                            v-model="formData.sourceOfIncome"
                                            type="text"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="e.g., Farming, Livestock"
                                        />
                                    </div>

                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">
                                            Estimated Annual Income (PHP)
                                        </label>
                                        <input
                                            v-model.number="formData.estimatedIncome"
                                            type="number"
                                            step="0.01"
                                            min="0"
                                            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                            placeholder="0.00"
                                        />
                                    </div>
                                </div>
                            </div>

                            <!-- Form Actions -->
                            <div class="sticky bottom-0 bg-white border-t border-gray-200 pt-6 -mx-6 px-6 pb-6">
                                <div class="flex justify-end space-x-3">
                                    <button
                                        type="button"
                                        @click="closeCreateModal"
                                        class="px-6 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        type="submit"
                                        :disabled="rsbsaStore.isLoading"
                                        class="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        {{ rsbsaStore.isLoading ? 'Creating...' : 'Create RSBSA' }}
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </transition>
            </div>
        </transition>

        <!-- Delete Confirmation Modal -->
        <transition
            enter-active-class="transition ease-out duration-300"
            enter-from-class="opacity-0"
            enter-to-class="opacity-100"
            leave-active-class="transition ease-in duration-200"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
        >
            <div
                v-if="showDeleteModal"
                class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
                @click="showDeleteModal = false"
            >
                <div
                    class="bg-white rounded-lg p-6 max-w-md w-full mx-4"
                    @click.stop
                >
                    <div class="flex items-center mb-4">
                        <div class="flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                            <AlertTriangle class="h-6 w-6 text-red-600" />
                        </div>
                        <h3 class="ml-4 text-lg font-medium text-gray-900">
                            Confirm Deletion
                        </h3>
                    </div>

                    <p class="text-sm text-gray-500 mb-6">
                        Are you sure you want to delete {{ selectedRsbsa.length }} RSBSA record(s)? This action cannot be undone.
                    </p>

                    <div class="flex justify-end space-x-3">
                        <button
                            @click="showDeleteModal = false"
                            class="px-4 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            @click="executeBulkDelete"
                            class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors"
                            :disabled="rsbsaStore.isLoading"
                        >
                            {{ rsbsaStore.isLoading ? 'Deleting...' : 'Delete' }}
                        </button>
                    </div>
                </div>
            </div>
        </transition>
    </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRsbsaStore } from '@/stores/rsbsa'
import { useNotificationStore } from '@/stores/notification'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { UNIFIED_NAVIGATION } from '@/lib/navigation'
import {
    Search,
    Filter,
    ChevronDown,
    Trash2,
    Database,
    ChevronLeft,
    ChevronRight,
    AlertTriangle,
    Plus,
    X,
    User,
    Phone,
    MapPin,
    Sprout,
    Wallet
} from 'lucide-vue-next'

const router = useRouter()
const rsbsaStore = useRsbsaStore()
const notificationStore = useNotificationStore()

const navigation = UNIFIED_NAVIGATION

// State
const searchQuery = ref('')
const showFilters = ref(false)
const selectedRsbsa = ref([])
const showDeleteModal = ref(false)
const showCreateModal = ref(false)
const currentPage = ref(1)
const perPage = ref(25)

const filters = ref({
    status: '',
    year: '',
    municipality: ''
})

const formData = ref({
    rsbsaId: '',
    firstName: '',
    lastName: '',
    middleName: '',
    gender: '',
    civilStatus: '',
    address: '',
    barangay: '',
    dateOfBirth: '',
    municipality: '',
    province: '',
    contactNumber: '',
    email: '',
    farmingType: '',
    primaryCrop: '',
    secondaryCrop: '',
    farmArea: null,
    farmLocation: '',
    tenureStatus: '',
    sourceOfIncome: '',
    estimatedIncome: 0,
    householdSize: null,
    educationLevel: '',
    withDisability: false
})

// Generate years for filter (last 10 years)
const currentYear = new Date().getFullYear()
const years = Array.from({ length: 10 }, (_, i) => currentYear - i)

// Computed
const filteredRsbsa = computed(() => {
    let result = rsbsaStore.allRsbsa

    // Search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(r =>
            r.rsbsaId?.toLowerCase().includes(query) ||
            r.firstName?.toLowerCase().includes(query) ||
            r.lastName?.toLowerCase().includes(query) ||
            r.middleName?.toLowerCase().includes(query) ||
            r.contactNumber?.toLowerCase().includes(query)
        )
    }

    // Status filter
    if (filters.value.status) {
        result = result.filter(r => r.status === filters.value.status)
    }

    // Year filter
    if (filters.value.year) {
        result = result.filter(r => {
            const regDate = new Date(r.registrationDate)
            return regDate.getFullYear() === parseInt(filters.value.year)
        })
    }

    // Municipality filter
    if (filters.value.municipality) {
        result = result.filter(r => r.municipality === filters.value.municipality)
    }

    return result
})

const totalPages = computed(() => Math.ceil(filteredRsbsa.value.length / perPage.value))

const paginatedRsbsa = computed(() => {
    const start = (currentPage.value - 1) * perPage.value
    const end = start + perPage.value
    return filteredRsbsa.value.slice(start, end)
})

const visiblePages = computed(() => {
    const pages = []
    const maxVisible = 5
    let startPage = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
    let endPage = Math.min(totalPages.value, startPage + maxVisible - 1)

    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(1, endPage - maxVisible + 1)
    }

    for (let i = startPage; i <= endPage; i++) {
        pages.push(i)
    }
    return pages
})

const isAllSelected = computed(() =>
    paginatedRsbsa.value.length > 0 &&
    paginatedRsbsa.value.every(r => selectedRsbsa.value.includes(r.rsbsaId))
)

// Methods
const formatFullName = (firstName, middleName, lastName) => {
    const parts = [firstName, middleName, lastName].filter(Boolean)
    return parts.join(' ') || 'N/A'
}

const formatLocation = (barangay, municipality) => {
    const parts = [barangay, municipality].filter(Boolean)
    return parts.join(', ') || 'N/A'
}

const toggleSelect = (rsbsaId) => {
    const index = selectedRsbsa.value.indexOf(rsbsaId)
    if (index > -1) {
        selectedRsbsa.value.splice(index, 1)
    } else {
        selectedRsbsa.value.push(rsbsaId)
    }
}

const toggleSelectAll = () => {
    if (isAllSelected.value) {
        // Deselect all on current page
        paginatedRsbsa.value.forEach(r => {
            const index = selectedRsbsa.value.indexOf(r.rsbsaId)
            if (index > -1) {
                selectedRsbsa.value.splice(index, 1)
            }
        })
    } else {
        // Select all on current page
        paginatedRsbsa.value.forEach(r => {
            if (!selectedRsbsa.value.includes(r.rsbsaId)) {
                selectedRsbsa.value.push(r.rsbsaId)
            }
        })
    }
}

const clearFilters = () => {
    filters.value = {
        status: '',
        year: '',
        municipality: ''
    }
}

const navigateToDetail = (rsbsaId) => {
    router.push({ name: 'rsbsa-detail', params: { id: rsbsaId } })
}

const confirmBulkDelete = () => {
    showDeleteModal.value = true
}

const executeBulkDelete = async () => {
    const result = await rsbsaStore.bulkDeleteRsbsa(selectedRsbsa.value)

    if (result.success) {
        notificationStore.showSuccess(result.message)
        selectedRsbsa.value = []
        showDeleteModal.value = false

        // Refresh the list
        await rsbsaStore.fetchAllRsbsa()
    } else {
        notificationStore.showError(result.message)
    }
}

const closeCreateModal = () => {
    showCreateModal.value = false
    // Reset form
    formData.value = {
        rsbsaId: '',
        firstName: '',
        lastName: '',
        middleName: '',
        gender: '',
        civilStatus: '',
        address: '',
        barangay: '',
        dateOfBirth: '',
        municipality: '',
        province: '',
        contactNumber: '',
        email: '',
        farmingType: '',
        primaryCrop: '',
        secondaryCrop: '',
        farmArea: null,
        farmLocation: '',
        tenureStatus: '',
        sourceOfIncome: '',
        estimatedIncome: 0,
        householdSize: null,
        educationLevel: '',
        withDisability: false
    }
}

const createRsbsa = async () => {
    const result = await rsbsaStore.createRsbsa(formData.value)

    if (result.success) {
        notificationStore.showSuccess('RSBSA record created successfully')
        closeCreateModal()

        // Refresh the list
        await rsbsaStore.fetchAllRsbsa()
    } else {
        notificationStore.showError(result.message || 'Failed to create RSBSA record')
    }
}

// Lifecycle
onMounted(async () => {
    await rsbsaStore.fetchAllRsbsa()
})
</script>

<style scoped>
/* Custom scrollbar for modal */
.overflow-y-auto::-webkit-scrollbar {
    width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
    background: #f1f5f9;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
    background: #94a3b8;
}
</style>
