<script setup>
import { ref, onMounted } from "vue";
import axiosInstance from "@/api/axiosInstance";

const message = ref("");

async function getMessage() {
  try {
    const response = await axiosInstance.get("/home");
    if (response.status === 200) message.value = response.data;
  } catch (e) {
    console.error(e.request);
    if (e.response) message.value = e.response.data;
    else if (e.request)
      message.value =
        "Unable to connect to the server. Please try again later.";
    else message.value = "An error occurred while processing your request.";
  }
}

onMounted(getMessage);
</script>

<template>
  <section class="py-5 mt-5">
    <div class="container">
      <h1 class="display-4 fw-bold mb-5">
        <span class="underline">Authenticated</span> Page
      </h1>
      <div>
        <h2>{{ message }}</h2>
      </div>
    </div>
  </section>
</template>
