<script setup>
import { ref, onMounted } from "vue";
import axiosInstance from "@/api/axiosInstance";

const message = ref("");

const getMessage = async () => {
  try {
    const response = await axiosInstance.get("/admin");
    console.info(response);
    if (response.status === 200) {
      message.value = response.data;
      console.info(response.data);
    }
  } catch (e) {
    if (e.response) {
      if (e.response.status === 403) {
        message.value = e.response.data;
      } else {
        message.value = e.response.data;
      }
    } else if (e.request) {
      console.error(e.request);
      message.value =
        "Unable to connect to the server. Please try again later.";
    } else {
      message.value = "An error occurred while processing your request.";
    }
  }
};

onMounted(getMessage);
</script>

<template>
  <section class="py-5 mt-5">
    <div class="container">
      <h1 class="display-4 fw-bold mb-5">
        <span class="underline">Admin</span> Page
      </h1>
      <div>
        <h2>{{ message }}</h2>
      </div>
    </div>
  </section>
</template>
