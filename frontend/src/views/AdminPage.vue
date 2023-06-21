<template>
  <section class="py-5 mt-5">
    <div class="container">
      <h1>Admin Page</h1>
      <div>
        <h2>{{ message }}</h2>
      </div>
    </div>
  </section>
</template>

<script setup>
import {ref, onMounted} from 'vue';
import axiosInstance from '@/api/axiosInstance';

const message = ref('');

const getMessage = async () => {
  try {
    const response = await axiosInstance.get('/admin');
    if (response.status === 200) {
      message.value = response.data
    }

  } catch (e) {
    if (e.response) {
      if (e.response.status === 401) {
        message.value = 'You are not authorized to view this page.';
      } else {
        message.value = e.response.data.message;
      }
    } else if (e.request) {
      message.value = 'Unable to connect to the server. Please try again later.';
    } else {
      message.value = 'An error occurred while processing your request.';
    }
  }
};

onMounted(getMessage);
</script>
