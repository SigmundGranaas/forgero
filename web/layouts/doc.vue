<template>
  <div class="flex flex-col min-h-screen">
    <!-- Header across the top of the page -->
    <Header/>
    <!-- Main content area -->
    <div class="flex flex-grow mx-auto">
      <aside class="w-1/4 flex-none p-4">
        <!-- Placeholder for structure -->
        Structure
      </aside>
      <main class="w-1/2 flex-grow max-w-main-content  p-4">
        <slot/>
      </main>
      <aside class="w-1/4 flex-none p-4">
        <!-- Placeholder for TOC -->
        TOC
      </aside>
    </div>
  </div>
</template>
<script setup lang="ts">
</script>
