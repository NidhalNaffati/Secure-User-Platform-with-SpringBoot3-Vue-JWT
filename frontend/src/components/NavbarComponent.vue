<script setup>
import axiosInstance from "@/api/axiosInstance";
import router from "@/router";
import {useAuthStore} from "@/stores";
import {computed, onMounted} from 'vue';

// Get the auth store instance
const authStore = useAuthStore();

// Use computed properties to automatically update when the store's state changes
const isAuthenticated = computed(() => authStore.isUserAuthenticated);
const isAdmin = computed(() => authStore.isAdmin);
const isUser = computed(() => authStore.isUser);

// Function to check if the user is authenticated and update the store state
const checkAuthenticationStateAndUpdateStore = () => {
  // Check if the tokens are removed from localStorage
  if (!localStorage.getItem('access_token') &&
      !localStorage.getItem('refresh_token')) {
    // if tokens are removed, that means the user is logged out
    authStore.logout();
  }
  // Check if the user is authenticated
  if (!authStore.isUserAuthenticated) {
    // if user is authenticated, that means the user is logged in
    // get the access token from localStorage
    const accessToken = localStorage.getItem('access_token');
    // extract the user role from the token
    const decodedToken = JSON.parse(atob(accessToken.split('.')[1]));
    const userRole = decodedToken.role;
    authStore.login(userRole);
  }
};

// Register the event listener when the component is mounted
onMounted(() => {
  checkAuthenticationStateAndUpdateStore();
});

const logout = async () => {
  try {
    // make a logout request to the server
    await axiosInstance.post('auth/logout');

    // remove the token from local storage
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');

    // reset the default store state
    authStore.logout();

    // redirect to the login page
    await router.push('/login');
  } catch (error) {
    console.error(error);
  }
};
</script>


<template>
  <!-- Start: Navbar Centered Links -->
  <nav class="navbar navbar-light navbar-expand-md fixed-top navbar-shrink py-3" id="mainNav">
    <div class="container">
      <router-link class="navbar-brand d-flex align-items-center" to="/"><span>Brand</span></router-link>
      <button data-bs-toggle="collapse" class="navbar-toggler" data-bs-target="#navcol-1"><span class="visually-hidden">Toggle navigation</span><span
          class="navbar-toggler-icon"></span></button>
      <div class="collapse navbar-collapse" id="navcol-1">
        <ul class="navbar-nav mx-auto">
          <li v-if="isAdmin" class="nav-item">
            <router-link class="nav-link" to="/admin">ADMIN</router-link>
          </li>
          <li v-if="isUser" class="nav-item">
            <router-link class="nav-link" to="/recording">USER</router-link>
          </li>
        </ul>
        <router-link v-if="!isAuthenticated" class="btn btn-outline-primary" to="/login">Log in</router-link>
        <router-link v-if="!isAuthenticated" class="btn btn-outline-secondary" to="/signup">Sign up</router-link>
        <router-link v-if="isAuthenticated" class="btn btn-outline-primary" @click="logout" to="/signup">Log out
        </router-link>
      </div>
    </div>
  </nav>
  <!-- End: Navbar Centered Links -->
</template>


<style scoped>

</style>
