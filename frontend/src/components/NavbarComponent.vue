<script setup>
import axiosInstance from "@/api/axiosInstance";
import router from "@/router";
import { useAuthStore } from "@/stores";
import { computed, onMounted, onUnmounted } from "vue";

// Get the auth store instance
const authStore = useAuthStore();

// Use computed properties to automatically update when the store's state changes
const isAuthenticated = computed(() => authStore.isUserAuthenticated);
const isAdmin = computed(() => authStore.isAdmin);
const isUser = computed(() => authStore.isUser);

// Function to check if the user is authenticated and update the store state
function checkAuthenticationStateAndUpdateStore() {
  // Check if the tokens are removed from localStorage
  if (
    !localStorage.getItem("access_token") &&
    !localStorage.getItem("refresh_token")
  ) {
    // if tokens are removed, that means the user is logged out
    // call the stores logout method this will update the stores state
    authStore.logout();
    router.push("/login");
  }
  // Check if the user is authenticated
  else if (!authStore.isUserAuthenticated) {
    // if user is authenticated, that means the user is logged in successfully
    // get the access token from localStorage
    const accessToken = localStorage.getItem("access_token");
    // extract the user role from the token
    const decodedToken = JSON.parse(atob(accessToken.split(".")[1]));
    const userRole = decodedToken.role;
    // call the stores login method this will update the stores state
    authStore.login(userRole);

    // update the authorization header
    axiosInstance.defaults.headers.common[
      "Authorization"
    ] = `Bearer ${accessToken}`;

    // redirect the user to the home page
    router.push("/");
  }
}

onMounted(() => {
  window.addEventListener("storage", checkAuthenticationStateAndUpdateStore);
});

onUnmounted(() => {
  window.removeEventListener("storage", checkAuthenticationStateAndUpdateStore);
});

const logout = async () => {
  try {
    // make a logout request to the server
    await axiosInstance.post("auth/logout");

    // remove the token from local storage
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");

    // reset the default store state
    authStore.logout();

    // Trigger localStorage event
    window.dispatchEvent(new Event("localStorage"));

    // redirect to the login page
    await router.push("/login");
  } catch (error) {
    console.error(error);
  }
};
</script>

<template>
  <!-- Start: Navbar Centered Links -->
  <nav
    class="navbar navbar-light navbar-expand-md fixed-top navbar-shrink py-3"
    id="mainNav"
  >
    <div class="container">
      <router-link class="navbar-brand d-flex align-items-center" to="/"
        ><span>Brand</span></router-link
      >
      <button
        data-bs-toggle="collapse"
        class="navbar-toggler"
        data-bs-target="#navcol-1"
      >
        <span class="visually-hidden">Toggle navigation</span
        ><span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navcol-1">
        <ul class="navbar-nav mx-auto">
          <li v-if="isAuthenticated" class="nav-item">
            <router-link class="nav-link" to="/authenticated"
              >Authenticated</router-link
            >
          </li>
          <li v-if="isAdmin" class="nav-item">
            <router-link class="nav-link" to="/admin">ADMIN</router-link>
          </li>
          <li v-if="isUser" class="nav-item">
            <router-link class="nav-link" to="/user">USER</router-link>
          </li>
        </ul>
        <router-link
          v-if="!isAuthenticated"
          class="btn btn-outline-primary"
          to="/login"
          >Log in</router-link
        >
        <router-link
          v-if="!isAuthenticated"
          class="btn btn-outline-secondary"
          to="/signup"
          >Sign up</router-link
        >
        <router-link
          v-if="isAuthenticated"
          class="btn btn-outline-primary"
          @click="logout"
          to="/signup"
          >Log out
        </router-link>
      </div>
    </div>
  </nav>
  <!-- End: Navbar Centered Links -->
</template>

<style scoped></style>
