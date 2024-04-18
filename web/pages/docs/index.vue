<template>
  <NuxtLayout>
    <ContentDoc v-slot="{ doc }">
      <article>
        <ContentRenderer :value="doc"/>
      </article>
    </ContentDoc>
  </NuxtLayout>

</template>

<script setup lang="ts">
definePageMeta({
  layout: 'doc'
})
</script>

