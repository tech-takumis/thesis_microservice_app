<template>
  <AuthenticatedLayout
    :navigation="navigation"
    role-title="Municipal Agriculturist"
    page-title="Program Detail">
    <div class="h-full flex flex-col min-h-0 overflow-hidden">
      <!-- Fixed Header Section -->
      <div class="flex-shrink-0 mb-4">
        <!-- Header with breadcrumb -->
        <nav class="flex mb-4" aria-label="Breadcrumb">
          <ol class="flex items-center space-x-4">
            <li>
              <div>
                <router-link
                  :to="{ name: 'agriculturist-dashboard' }"
                  class="text-gray-400 hover:text-gray-500"
                >
                  <HomeIcon class="flex-shrink-0 h-5 w-5" />
                  <span class="sr-only">Dashboard</span>
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <router-link
                  :to="{ name: 'agriculturist-monitor-programs' }"
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  Monitor Programs
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <span class="ml-4 text-sm font-medium text-black">
                  Program Details
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <div class="flex items-center justify-between ml-5">
          <div>
            <h1 class="text-3xl font-bold text-green-600">Program Details</h1>
            <p class="mt-1 text-sm text-gray-600">
              {{ program ? program.name : 'Loading...' }}
            </p>
          </div>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="flex-1 min-h-0 overflow-hidden">
        <div class="h-full">
          <!-- Loading State -->
          <div v-if="loading" class="flex justify-center items-center flex-1 h-full">
            <LoadingSpinner />
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
            <div class="flex">
              <div class="flex-shrink-0">
                <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-800">Error Loading Program Details</h3>
                <div class="mt-2 text-sm text-red-700">{{ error }}</div>
              </div>
            </div>
          </div>

          <!-- Program Details -->
          <div v-else-if="program" class="bg-gray-50 rounded-xl shadow-sm p-3 h-full overflow-auto">
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
              <!-- Left Column - Main Information -->
              <div class="lg:col-span-2 space-y-4">
                <!-- Program Information Card -->
                <div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                  <div class="px-4 py-4">
                    <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
                      <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                      </svg>
                      Program Information
                    </h2>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-3">
                      <!-- Program ID -->
                      <div class="flex flex-col">
                        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                          Program ID
                        </span>
                        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                          <span class="text-sm text-gray-900 font-medium break-words font-mono">
                            {{ program.id }}
                          </span>
                        </div>
                      </div>

                      <!-- Program Name -->
                      <div class="flex flex-col">
                        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                          Program Name
                        </span>
                        <div v-if="editingField !== 'name'" class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150 flex justify-between items-center group">
                          <span class="text-sm text-gray-900 font-medium break-words">
                            {{ program.name }}
                          </span>
                          <button
                            @click="startEdit('name')"
                            class="opacity-0 group-hover:opacity-100 transition-opacity text-green-600 hover:text-green-700 ml-2"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                            </svg>
                          </button>
                        </div>
                        <div v-else class="flex gap-2">
                          <input
                            v-model="editValues.name"
                            type="text"
                            class="flex-1 px-3 py-2 border border-green-500 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                            @keyup.enter="updateField('name')"
                            @keyup.esc="cancelEdit"
                            autofocus
                          />
                          <button
                            @click="updateField('name')"
                            class="px-3 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                            </svg>
                          </button>
                          <button
                            @click="cancelEdit"
                            class="px-3 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                            </svg>
                          </button>
                        </div>
                      </div>

                      <!-- Program Type -->
                      <div class="flex flex-col">
                        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                          Program Type
                        </span>
                        <div v-if="editingField !== 'type'" class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150 flex justify-between items-center group">
                          <span :class="getTypeColor(program.type)" class="px-2 py-1 text-xs font-medium rounded-full">
                            {{ program.type }}
                          </span>
                          <button
                            @click="startEdit('type')"
                            class="opacity-0 group-hover:opacity-100 transition-opacity text-green-600 hover:text-green-700 ml-2"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                            </svg>
                          </button>
                        </div>
                        <div v-else class="flex gap-2">
                          <select
                            v-model="editValues.type"
                            class="flex-1 px-3 py-2 border border-green-500 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                            @change="updateField('type')"
                            autofocus
                          >
                            <option value="TRAINING">Training</option>
                            <option value="DISTRIBUTION">Distribution</option>
                            <option value="MONITORING">Monitoring</option>
                            <option value="CONSULTATION">Consultation</option>
                          </select>
                          <button
                            @click="cancelEdit"
                            class="px-3 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                            </svg>
                          </button>
                        </div>
                      </div>

                      <!-- Status -->
                      <div class="flex flex-col">
                        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                          Status
                        </span>
                        <div v-if="editingField !== 'status'" class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150 flex justify-between items-center group">
                          <span :class="getStatusColor(program.status)" class="px-2 py-1 text-xs font-medium rounded-full">
                            {{ program.status }}
                          </span>
                          <button
                            @click="startEdit('status')"
                            class="opacity-0 group-hover:opacity-100 transition-opacity text-green-600 hover:text-green-700 ml-2"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                            </svg>
                          </button>
                        </div>
                        <div v-else class="flex gap-2">
                          <select
                            v-model="editValues.status"
                            class="flex-1 px-3 py-2 border border-green-500 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                            @change="updateField('status')"
                            autofocus
                          >
                            <option value="PENDING">Pending</option>
                            <option value="ACTIVE">Active</option>
                            <option value="COMPLETED">Completed</option>
                            <option value="INACTIVE">Inactive</option>
                            <option value="CANCELLED">Cancelled</option>
                          </select>
                          <button
                            @click="cancelEdit"
                            class="px-3 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                            </svg>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Program Notes Card -->
                <div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                  <div class="px-4 py-4">
                    <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center justify-between">
                      <span class="flex items-center">
                        <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                        Program Notes
                      </span>
                      <button
                        v-if="editingField !== 'notes'"
                        @click="startEdit('notes')"
                        class="text-green-600 hover:text-green-700"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                      </button>
                    </h2>

                    <div v-if="editingField !== 'notes'" class="w-full bg-white rounded-md px-4 py-3 border border-gray-200 shadow-sm">
                      <p class="text-sm text-gray-900 whitespace-pre-wrap">
                        {{ program.notes || 'No notes available for this program.' }}
                      </p>
                    </div>
                    <div v-else class="space-y-2">
                      <textarea
                        v-model="editValues.notes"
                        rows="6"
                        class="w-full px-4 py-3 border border-green-500 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                        placeholder="Enter program notes..."
                        autofocus
                      ></textarea>
                      <div class="flex justify-end gap-2">
                        <button
                          @click="updateField('notes')"
                          class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors text-sm flex items-center"
                        >
                          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                          </svg>
                          Save
                        </button>
                        <button
                          @click="cancelEdit"
                          class="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors text-sm"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Timeline Card -->
                <div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                  <div class="px-4 py-4">
                    <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
                      <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                      </svg>
                      Timeline
                    </h2>

                    <div class="space-y-3">
                      <!-- Created At -->
                      <div class="flex items-start">
                        <div class="flex-shrink-0">
                          <div class="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center">
                            <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                            </svg>
                          </div>
                        </div>
                        <div class="ml-4 flex-1">
                          <p class="text-xs font-semibold text-green-600 uppercase tracking-wider">Created</p>
                          <p class="text-sm text-gray-900 font-medium mt-1">{{ formatDate(program.createdAt) }}</p>
                          <p class="text-xs text-gray-500 mt-0.5">{{ getRelativeTime(program.createdAt) }}</p>
                        </div>
                      </div>

                      <!-- Updated At -->
                      <div class="flex items-start">
                        <div class="flex-shrink-0">
                          <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                            <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                            </svg>
                          </div>
                        </div>
                        <div class="ml-4 flex-1">
                          <p class="text-xs font-semibold text-blue-600 uppercase tracking-wider">Last Updated</p>
                          <p class="text-sm text-gray-900 font-medium mt-1">{{ formatDate(program.updatedAt) }}</p>
                          <p class="text-xs text-gray-500 mt-0.5">{{ getRelativeTime(program.updatedAt) }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Right Column - Stats & Progress -->
              <div class="space-y-4">
                <!-- Completion Progress Card -->
                <div class="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl border border-green-200 shadow-sm">
                  <div class="px-4 py-4">
                    <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center justify-between">
                      <span class="flex items-center">
                        <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                        </svg>
                        Completion Progress
                      </span>
                      <button
                        v-if="editingField !== 'completion'"
                        @click="startEdit('completion')"
                        class="text-green-600 hover:text-green-700"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                      </button>
                    </h2>

                    <div v-if="editingField !== 'completion'" class="text-center">
                      <div class="relative inline-flex items-center justify-center">
                        <svg class="w-32 h-32 transform -rotate-90">
                          <circle
                            cx="64"
                            cy="64"
                            r="56"
                            stroke="currentColor"
                            stroke-width="8"
                            fill="none"
                            class="text-gray-200"
                          />
                          <circle
                            cx="64"
                            cy="64"
                            r="56"
                            stroke="currentColor"
                            stroke-width="8"
                            fill="none"
                            :stroke-dasharray="circumference"
                            :stroke-dashoffset="dashOffset"
                            class="text-green-600 transition-all duration-1000 ease-out"
                            stroke-linecap="round"
                          />
                        </svg>
                        <div class="absolute inset-0 flex items-center justify-center">
                          <div class="text-center">
                            <span :class="getCompletionColor(program.completion)" class="text-3xl font-bold">
                              {{ program.completion }}%
                            </span>
                            <p class="text-xs text-gray-600 mt-1">Complete</p>
                          </div>
                        </div>
                      </div>

                      <div class="mt-4 pt-4 border-t border-green-200">
                        <div class="flex justify-between text-sm">
                          <span class="text-gray-600">Remaining:</span>
                          <span class="font-medium text-gray-900">{{ 100 - program.completion }}%</span>
                        </div>
                      </div>
                    </div>
                    <div v-else class="space-y-4">
                      <div class="text-center">
                        <div class="relative inline-flex items-center justify-center">
                          <svg class="w-32 h-32 transform -rotate-90">
                            <circle
                              cx="64"
                              cy="64"
                              r="56"
                              stroke="currentColor"
                              stroke-width="8"
                              fill="none"
                              class="text-gray-200"
                            />
                            <circle
                              cx="64"
                              cy="64"
                              r="56"
                              stroke="currentColor"
                              stroke-width="8"
                              fill="none"
                              :stroke-dasharray="circumference"
                              :stroke-dashoffset="circumference * (1 - editValues.completion / 100)"
                              class="text-green-600 transition-all duration-300 ease-out"
                              stroke-linecap="round"
                            />
                          </svg>
                          <div class="absolute inset-0 flex items-center justify-center">
                            <div class="text-center">
                              <span :class="getCompletionColor(editValues.completion)" class="text-3xl font-bold">
                                {{ editValues.completion }}%
                              </span>
                              <p class="text-xs text-gray-600 mt-1">Complete</p>
                            </div>
                          </div>
                        </div>
                      </div>

                      <div class="space-y-3">
                        <input
                          v-model.number="editValues.completion"
                          type="range"
                          min="0"
                          max="100"
                          class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-green-600"
                        />
                        <div class="flex justify-between text-xs text-gray-600">
                          <span>0%</span>
                          <span>50%</span>
                          <span>100%</span>
                        </div>
                      </div>

                      <div class="flex justify-end gap-2 pt-2">
                        <button
                          @click="updateField('completion')"
                          class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors text-sm flex items-center"
                        >
                          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                          </svg>
                          Save
                        </button>
                        <button
                          @click="cancelEdit"
                          class="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors text-sm"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Quick Stats Card -->
                <div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                  <div class="px-4 py-4">
                    <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
                      <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                      </svg>
                      Quick Stats
                    </h2>

                    <div class="space-y-3">
                      <!-- Type Info -->
                      <div class="bg-white rounded-lg p-3 border border-gray-200">
                        <div class="flex items-center justify-between">
                          <span class="text-xs font-medium text-gray-600">Type</span>
                          <span :class="getTypeColor(program.type)" class="px-2 py-1 text-xs font-medium rounded-full">
                            {{ program.type }}
                          </span>
                        </div>
                        <p class="text-xs text-gray-500 mt-1">{{ getTypeDescription(program.type) }}</p>
                      </div>

                      <!-- Status Info -->
                      <div class="bg-white rounded-lg p-3 border border-gray-200">
                        <div class="flex items-center justify-between">
                          <span class="text-xs font-medium text-gray-600">Current Status</span>
                          <span :class="getStatusColor(program.status)" class="px-2 py-1 text-xs font-medium rounded-full">
                            {{ program.status }}
                          </span>
                        </div>
                        <p class="text-xs text-gray-500 mt-1">{{ getStatusDescription(program.status) }}</p>
                      </div>

                      <!-- Duration -->
                      <div class="bg-white rounded-lg p-3 border border-gray-200">
                        <div class="flex items-center justify-between">
                          <span class="text-xs font-medium text-gray-600">Active Duration</span>
                          <span class="text-sm font-bold text-gray-900">{{ getDuration(program.createdAt, program.updatedAt) }}</span>
                        </div>
                        <p class="text-xs text-gray-500 mt-1">Time since creation</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex justify-end gap-3 px-2 mt-4">
              <button
                @click="goBack"
                class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-colors"
              >
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
                </svg>
                Back to Programs
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Notification Toast -->
    <NotificationToast />
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeIcon, ChevronRightIcon, ExclamationTriangleIcon } from '@heroicons/vue/24/outline'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import NotificationToast from '@/components/others/NotificationToast.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useProgramStore } from '@/stores/program.js'
import { useNotificationStore } from '@/stores/notification.js'

const route = useRoute()
const router = useRouter()
const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const programStore = useProgramStore()
const notificationStore = useNotificationStore()

// State
const program = ref(null)
const loading = ref(false)
const error = ref(null)

// Edit states
const editingField = ref(null)
const editValues = ref({
  name: '',
  type: '',
  status: '',
  notes: '',
  completion: 0
})

// Computed for circular progress
const circumference = computed(() => 2 * Math.PI * 56)
const dashOffset = computed(() => {
  if (!program.value) return circumference.value
  const progress = program.value.completion / 100
  return circumference.value * (1 - progress)
})

// Methods
const fetchProgramDetail = async () => {
  try {
    loading.value = true
    error.value = null

    const programId = route.params.id
    const result = await programStore.getProgram(programId)

    if (result.success) {
      program.value = result.data
    } else {
      error.value = result.message || 'Failed to load program details'
    }
  } catch (err) {
    console.error('Error fetching program:', err)
    error.value = err.message || 'An unexpected error occurred'
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    return new Date(dateString).toLocaleDateString('en-PH', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return 'Invalid Date'
  }
}

const getRelativeTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return 'Today'
  if (diffDays === 1) return 'Yesterday'
  if (diffDays < 7) return `${diffDays} days ago`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`
  if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`
  return `${Math.floor(diffDays / 365)} years ago`
}

const getDuration = (startDate, endDate) => {
  if (!startDate) return 'N/A'
  const start = new Date(startDate)
  const end = endDate ? new Date(endDate) : new Date()
  const diffMs = end - start
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays < 1) return 'Less than a day'
  if (diffDays === 1) return '1 day'
  if (diffDays < 30) return `${diffDays} days`
  if (diffDays < 365) return `${Math.floor(diffDays / 30)} months`
  return `${Math.floor(diffDays / 365)} years`
}

const getStatusColor = (status) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'bg-blue-100 text-blue-800'
    case 'COMPLETED':
      return 'bg-green-100 text-green-800'
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

const getTypeColor = (type) => {
  switch (type?.toUpperCase()) {
    case 'TRAINING':
      return 'bg-purple-100 text-purple-800'
    case 'DISTRIBUTION':
      return 'bg-green-100 text-green-800'
    case 'MONITORING':
      return 'bg-blue-100 text-blue-800'
    case 'CONSULTATION':
      return 'bg-orange-100 text-orange-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getCompletionColor = (percentage) => {
  if (percentage >= 80) return 'text-green-600'
  if (percentage >= 50) return 'text-yellow-600'
  return 'text-red-600'
}

const getTypeDescription = (type) => {
  switch (type?.toUpperCase()) {
    case 'TRAINING':
      return 'Educational and skill development program'
    case 'DISTRIBUTION':
      return 'Resource and material distribution'
    case 'MONITORING':
      return 'Tracking and oversight activities'
    case 'CONSULTATION':
      return 'Advisory and guidance services'
    default:
      return 'Agricultural program'
  }
}

const getStatusDescription = (status) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'Program is currently running'
    case 'COMPLETED':
      return 'Program has been finished'
    case 'PENDING':
      return 'Awaiting approval or start'
    case 'INACTIVE':
      return 'Program is not currently active'
    case 'CANCELLED':
      return 'Program has been terminated'
    default:
      return 'Status information'
  }
}

const startEdit = (field) => {
  editingField.value = field
  editValues.value[field] = program.value[field]
}

const cancelEdit = () => {
  editingField.value = null
}

const updateField = async (field) => {
  try {
    // Ensure all required fields have valid values
    const updateData = {
      name: program.value.name || '',
      type: program.value.type || 'TRAINING',
      status: program.value.status || 'PENDING',
      notes: program.value.notes || '',
      completion: typeof program.value.completion === 'number' ? program.value.completion : 0
    }

    // Update the specific field with the new value
    updateData[field] = editValues.value[field]

    // Ensure the updated field also has a valid value
    if (field === 'notes' && !updateData[field]) {
      updateData[field] = ''
    }
    if (field === 'completion') {
      updateData[field] = typeof updateData[field] === 'number' ? updateData[field] : 0
    }

    console.log('Updating program with data:', updateData)

    const result = await programStore.updateProgram(program.value.id, updateData)

    if (result.success) {
      program.value = result.data
      editingField.value = null
      notificationStore.showSuccess(`${field.charAt(0).toUpperCase() + field.slice(1)} updated successfully!`)
    } else {
      notificationStore.showError(`Failed to update ${field}: ${result.message}`)
      editValues.value[field] = program.value[field]
    }
  } catch (err) {
    console.error('Error updating program:', err)
    notificationStore.showError(`An error occurred while updating ${field}`)
    editValues.value[field] = program.value[field]
  }
}

const goBack = () => {
  router.push({ name: 'agriculturist-monitor-programs' })
}

// Lifecycle
onMounted(async () => {
  await fetchProgramDetail()
})
</script>

<style scoped>
/* Custom scrollbar styling */
.overflow-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-auto::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.overflow-auto::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.overflow-auto::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.break-words {
  word-wrap: break-word;
  word-break: break-word;
}
</style>
