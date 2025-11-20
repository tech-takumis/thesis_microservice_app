# PCIC Design System

This document defines the design system used in the PCIC application based on the ApplicationInspection component. Use these patterns consistently across all pages and components.

## Color Palette

### Background Colors
- **Page Background**: `bg-gradient-to-br from-slate-50 to-slate-100/50`
  - Clean gradient background for main pages
- **Card Background**: `bg-white/70 backdrop-blur-sm`
  - Semi-transparent white with backdrop blur for glassmorphism effect
- **Upload Area**: `bg-slate-50/50`
  - Subtle background for upload zones
- **Hover Upload**: `hover:bg-slate-100/50`
- **Modal Overlay**: `bg-black/30 backdrop-blur-sm`
- **Modal Background**: `bg-white`

### State Backgrounds
- **Error State**: `bg-red-50/50` with `border-red-200/60`
- **Success/Approved**: `bg-emerald-100` with `text-emerald-700`
- **Warning/Pending**: `bg-amber-100` with `text-amber-700`
- **High Priority**: `bg-red-100` with `text-red-700`

### Text Colors
- **Primary Text**: `text-slate-900` - Main headings and important text
- **Secondary Text**: `text-slate-700` - Subheadings and labels
- **Tertiary Text**: `text-slate-600` - Supporting text
- **Muted Text**: `text-slate-500` - Descriptions and helper text
- **Disabled Text**: `text-slate-400` - Disabled state text
- **Light Text**: `text-slate-300` - Icons and dividers
- **Error Text**: `text-red-900`, `text-red-700/80`
- **Success Text**: `text-emerald-700`
- **Warning Text**: `text-amber-700`

## Typography

### Headings
```html
<!-- Page Title (H1) -->
<h1 class="text-2xl font-light text-slate-900 tracking-tight">Page Title</h1>

<!-- Section Heading (H3) -->
<h3 class="text-sm font-medium text-slate-700">Section Title</h3>

<!-- Subsection Heading (H4) -->
<h4 class="text-sm font-semibold text-slate-800 uppercase tracking-wider">Subsection</h4>
```

### Body Text
- **Standard**: `text-sm text-slate-900`
- **Description**: `text-sm text-slate-500`
- **Small Text**: `text-xs text-slate-500`

### Labels
```html
<label class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
  Field Label
</label>
```

### Font Weights
- **Light**: `font-light` - Page titles
- **Regular**: (default) - Body text
- **Medium**: `font-medium` - Section headings, buttons
- **Semibold**: `font-semibold` - Labels, important text

## Cards & Containers

### Card Component
```html
<div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
  <!-- Card Header -->
  <div class="px-6 py-4 border-b border-slate-100/80">
    <h3 class="text-sm font-medium text-slate-700">Card Title</h3>
    <p class="mt-0.5 text-xs text-slate-500">Card description</p>
  </div>

  <!-- Card Body -->
  <div class="p-6">
    <!-- Content here -->
  </div>
</div>
```

### Card Properties
- **Border Radius**: `rounded-2xl` (16px)
- **Border**: `border border-slate-200/60`
- **Shadow**: `shadow-sm`
- **Padding**: `p-6` for body, `px-6 py-4` for header
- **Header Border**: `border-b border-slate-100/80`

### Page Container
```html
<div class="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100/50">
  <div class="max-w-7xl mx-auto px-8 py-8 sm:px-12 lg:px-16">
    <!-- Page content -->
  </div>
</div>
```

## Form Elements

### Text Input
```html
<input
  type="text"
  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200"
/>
```

### Number Input
```html
<input
  type="number"
  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200"
/>
```

### Date Input
```html
<input
  type="date"
  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200"
/>
```

### Textarea
```html
<textarea
  rows="4"
  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium resize-none placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200"
></textarea>
```

### Checkbox
```html
<input
  type="checkbox"
  class="w-5 h-5 rounded-md border-2 border-slate-300 text-blue-500 focus:ring-4 focus:ring-blue-400/20 focus:border-blue-400 cursor-pointer transition-all duration-200"
/>
```

### Readonly/Disabled State
```html
<input
  readonly
  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-slate-50 text-slate-500 cursor-not-allowed"
/>
```

### Form Layout
```html
<!-- Two Column Grid -->
<div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
  <div>
    <label class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
      Field Label
    </label>
    <input type="text" class="..." />
  </div>
</div>
```

## Buttons

### Primary Button
```html
<button class="inline-flex items-center px-6 py-2.5 border border-transparent text-sm font-medium rounded-xl text-white bg-blue-500 hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-200">
  <CheckCircleIcon class="w-4 h-4 mr-2" />
  Button Text
</button>
```

### Danger Button
```html
<button class="p-1.5 bg-red-500 hover:bg-red-600 text-white rounded-full transition-all duration-200 shadow-lg">
  <XMarkIcon class="h-4 w-4" />
</button>
```

### Icon Button
```html
<button class="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50/50 rounded-full transition-all duration-200">
  <InformationCircleIcon class="h-5 w-5" />
</button>
```

### Disabled State
```html
<button
  disabled
  class="... disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-none"
>
  Button Text
</button>
```

## Navigation & Breadcrumbs

### Breadcrumb Navigation
```html
<nav class="flex mb-8" aria-label="Breadcrumb">
  <ol class="flex items-center space-x-1.5">
    <li>
      <router-link class="text-slate-400 hover:text-slate-700 transition-colors duration-200">
        <HomeIcon class="h-4 w-4" />
      </router-link>
    </li>
    <li class="flex items-center">
      <ChevronRightIcon class="h-3 w-3 text-slate-300 mx-1" />
      <button class="text-xs font-medium text-slate-500 hover:text-slate-700 transition-colors duration-200">
        Parent
      </button>
    </li>
    <li class="flex items-center">
      <ChevronRightIcon class="h-3 w-3 text-slate-300 mx-1" />
      <span class="text-xs font-medium text-slate-900">Current</span>
    </li>
  </ol>
</nav>
```

## Status Badges

### Status Badge Component
```html
<!-- Success/Approved -->
<span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium bg-emerald-100 text-emerald-700">
  Approved
</span>

<!-- Warning/Pending -->
<span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium bg-amber-100 text-amber-700">
  Pending
</span>

<!-- Error/Rejected -->
<span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium bg-red-100 text-red-700">
  Rejected
</span>
```

## Upload Components

### File Upload Zone
```html
<label class="flex flex-col items-center justify-center w-full h-32 border-2 border-slate-200 border-dashed rounded-xl cursor-pointer bg-slate-50/50 hover:bg-slate-100/50 hover:border-slate-300 transition-all duration-200">
  <div class="flex flex-col items-center justify-center py-4">
    <PhotoIcon class="w-10 h-10 mb-2 text-slate-400" />
    <p class="mb-1 text-sm text-slate-600">
      <span class="font-medium">Click to upload</span> or drag and drop
    </p>
    <p class="text-xs text-slate-500">PNG, JPG, JPEG up to 10MB</p>
  </div>
  <input type="file" class="hidden" accept="image/*" multiple />
</label>
```

### Image Preview Grid
```html
<div class="grid grid-cols-2 md:grid-cols-4 gap-3">
  <div class="relative group aspect-square rounded-xl overflow-hidden bg-slate-100 border border-slate-200 hover:border-slate-300 transition-all duration-200">
    <img src="..." class="w-full h-full object-cover" />
    <!-- Delete Button -->
    <button class="absolute top-2 right-2 p-1.5 bg-red-500 hover:bg-red-600 text-white rounded-full opacity-0 group-hover:opacity-100 transition-all duration-200 shadow-lg">
      <XMarkIcon class="h-4 w-4" />
    </button>
  </div>
</div>
```

## Modals

### Modal Overlay
```html
<div class="fixed inset-0 bg-black/30 backdrop-blur-sm z-40"></div>
```

### Side Modal (Slide from Right)
```html
<div class="fixed inset-y-0 right-0 z-50 w-96 bg-white shadow-2xl overflow-y-auto">
  <!-- Modal Header -->
  <div class="sticky top-0 bg-white border-b border-slate-200 px-6 py-5">
    <div class="flex items-center justify-between">
      <h3 class="text-lg font-medium text-slate-900">Modal Title</h3>
      <button class="text-slate-400 hover:text-slate-600 hover:bg-slate-100 p-1.5 rounded-lg transition-all duration-200">
        <XMarkIcon class="h-5 w-5" />
      </button>
    </div>
  </div>

  <!-- Modal Body -->
  <div class="p-6 space-y-5">
    <!-- Content -->
  </div>
</div>
```

### Full Screen Image Modal
```html
<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-md p-4">
  <button class="absolute top-6 right-6 p-2.5 rounded-full bg-white/10 hover:bg-white/20 text-white transition-all duration-200">
    <XMarkIcon class="h-6 w-6" />
  </button>
  <img src="..." class="max-w-full max-h-full object-contain rounded-2xl shadow-2xl" />
</div>
```

## Loading & Empty States

### Loading State
```html
<div class="flex justify-center items-center py-20">
  <LoadingSpinner message="Loading data..." />
</div>
```

### Error State
```html
<div class="bg-red-50/50 border border-red-200/60 rounded-xl p-5 backdrop-blur-sm">
  <div class="flex">
    <ExclamationTriangleIcon class="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
    <div class="ml-3">
      <h3 class="text-sm font-medium text-red-900">Error Title</h3>
      <p class="mt-1 text-sm text-red-700/80">Error message</p>
    </div>
  </div>
</div>
```

### Empty State
```html
<div class="text-center py-12">
  <DocumentIcon class="mx-auto h-12 w-12 text-slate-300" />
  <p class="mt-3 text-sm text-slate-500">No data available</p>
</div>
```

## Spacing System

### Container Spacing
- **Page Padding**: `px-8 py-8 sm:px-12 lg:px-16`
- **Max Width**: `max-w-7xl mx-auto`

### Section Spacing
- **Between Sections**: `space-y-6`
- **Between Form Rows**: `gap-y-5`
- **Between Form Columns**: `gap-x-6`
- **Margin Bottom**: `mb-6`, `mb-8` for major sections

### Internal Spacing
- **Card Padding**: `p-6`
- **Card Header**: `px-6 py-4`
- **Button Padding**: `px-6 py-2.5` (medium), `px-4 py-2` (small)
- **Input Padding**: `px-4 py-3`

## Border Radius

- **Large Cards**: `rounded-2xl` (16px)
- **Inputs/Buttons**: `rounded-xl` (12px)
- **Small Elements**: `rounded-lg` (8px)
- **Checkboxes**: `rounded-md` (6px)
- **Icons/Avatars**: `rounded-full`

## Shadows

- **Cards**: `shadow-sm`
- **Buttons on Hover**: `hover:shadow-lg hover:shadow-blue-500/30`
- **Modals**: `shadow-2xl`
- **Images**: `shadow-2xl`

## Transitions

Standard transition for all interactive elements:
```
transition-all duration-200
```

Specific transitions:
- **Colors**: `transition-colors duration-200`
- **Transforms**: `transition-transform duration-200`

## Focus States

All interactive elements should have focus states:
```
focus:outline-none focus:ring-4 focus:ring-blue-400/10 focus:border-blue-400
```

## Hover States

- **Links**: `hover:text-slate-700`
- **Buttons**: `hover:bg-blue-600 hover:shadow-lg`
- **Cards**: `hover:border-slate-300`
- **Icons**: `hover:text-blue-600 hover:bg-blue-50/50`

## Grid Layouts

### Two Column Form
```html
<div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
  <!-- Form fields -->
</div>
```

### Four Column Grid (Images)
```html
<div class="grid grid-cols-2 md:grid-cols-4 gap-3">
  <!-- Items -->
</div>
```

## Icon Sizes

- **Small Icons**: `h-3 w-3` (breadcrumb chevrons)
- **Standard Icons**: `h-4 w-4` (button icons)
- **Medium Icons**: `h-5 w-5` (action buttons, modal close)
- **Large Icons**: `h-6 w-6` (modal close on dark backgrounds)
- **Extra Large Icons**: `h-10 w-10` to `h-16 w-16` (empty states, upload areas)

## Best Practices

1. **Always use the slate color palette** for text and backgrounds (not gray)
2. **Use blue-500/600** for primary actions and focus states
3. **Apply backdrop-blur-sm** with semi-transparent backgrounds for glassmorphism
4. **Use transition-all duration-200** for smooth interactions
5. **Add focus rings** with `focus:ring-4 focus:ring-blue-400/10`
6. **Implement proper hover states** on all interactive elements
7. **Use uppercase tracking-wider** for labels
8. **Apply font-medium** for buttons and important text
9. **Use rounded-xl or rounded-2xl** for modern, friendly borders
10. **Implement proper disabled states** with opacity-50 and cursor-not-allowed

## Accessibility

- Always include proper ARIA labels
- Use semantic HTML elements
- Ensure sufficient color contrast
- Provide focus indicators
- Use meaningful alt text for images
- Include screen reader only text where needed: `<span class="sr-only">Text</span>`
