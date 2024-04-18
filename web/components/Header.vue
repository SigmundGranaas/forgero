<template>
  <header class="bg-gray-800 text-white p-4 flex items-center w-full">
    <div class="max-w-main-content w-full mx-auto flex justify-between items-center">
      <!-- Logo on the left -->
      <div class="flex items-center">
        <span class="text-lg font-bold">Forgero</span>
      </div>

      <!-- Navigation links in the middle -->
      <nav class="flex-grow text-center flex justify-center items-center">
        <a href="/getting-started" class="mx-2 hover:text-gray-300">Getting started</a>
        <a href="/api" class="mx-2 hover:text-gray-300">API</a>
        <a href="/releases" class="mx-2 hover:text-gray-300">Releases</a>
      </nav>

      <!-- Social media icons on the right -->
      <div class="flex items-center">
        <!-- Example icons, replace with actual icons or images -->
        <a href="#" class="mx-2 hover:text-gray-300">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M18.364 5.636a9 9 0 1012.728 12.728 9 9 0 00-12.728-12.728zM15 9a3 3 0 11-6 0 3 3 0 016 0zm-3 9h.01"></path>
          </svg>
        </a>
        <a href="#" class="mx-2 hover:text-gray-300">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M17 8h2a2 2 0 012 2v7a2 2 0 01-2 2h-2m0 0a2 2 0 01-2-2v-7a2 2 0 012-2m0 0V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v3m8 10v1a3 3 0 01-3 3H9a3 3 0 01-3-3v-1m3 0h4"></path>
          </svg>
        </a>
      </div>
    </div>
  </header>
</template>
