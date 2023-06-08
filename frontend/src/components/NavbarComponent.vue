<script>
import axiosInstance from "@/api/axiosInstance";
import router from "@/router";
import {useAuthStore} from "@/stores";
import {computed} from 'vue';

export default {
  name: "NavbarComponent",
  setup() {
    const authStore = useAuthStore();

    // Use computed properties to automatically update when the store's state changes
    const isAuthenticated = computed(() => authStore.isUserAuthenticated);
    const isAdmin = computed(() => authStore.isAdmin);
    const isUser = computed(() => authStore.isUser);

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

    return {
      isAuthenticated,
      isAdmin,
      isUser,
      logout
    };
  }
};
</script>


<template>
  <!-- Start: Navbar Centered Links -->
  <nav class="navbar navbar-light navbar-expand-md fixed-top navbar-shrink py-3" id="mainNav">
    <div class="container"><a class="navbar-brand d-flex align-items-center" href="/"><span>Brand</span></a>
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
