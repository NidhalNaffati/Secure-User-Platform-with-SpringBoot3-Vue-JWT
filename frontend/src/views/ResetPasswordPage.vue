<script setup>
import { ref } from "vue";
import { useRoute } from "vue-router";
import axiosInstance from "@/api/axiosInstance";
import router from "@/router";

// declare a reactive data to hold the password & the confirmPassword
const password = ref("");
const passwordConfirm = ref("");
const token = ref("");

const errorMessage = ref("");
// declare the route to get the token from the url
const $route = useRoute();
// get the token from the url
token.value = $route.query.token;

async function resetPassword() {
  try {
    const response = await axiosInstance.post("auth/reset-password", {
      token: token.value,
      password: password.value,
      passwordConfirm: passwordConfirm.value,
    });

    if (response.status === 200) await router.push("/login");
  } catch (error) {
    console.log("error: ", error.response.data);
    if (error.response.status === 422)
      showErrorMessage(error.response.data.message);
    else showErrorMessage(error.response.data);
  }
}

function showErrorMessage(message) {
  errorMessage.value = message;
}
</script>

<template>
  <section class="py-4 py-md-5 mt-5">
    <div class="container py-md-5">
      <div class="row d-flex align-items-center">
        <div class="col-md-6 text-center">
          <img
            class="img-fluid w-100"
            src="src/assets/img/illustrations/desk.svg"
            alt="forgotten-password-img"
          />
        </div>
        <div class="col-md-5 col-xl-4 text-center text-md-start">
          <h2 class="display-6 fw-bold mb-4">
            Reset your <span class="underline">password</span>?
          </h2>
          <p class="text-muted">Enter the password & the confirm password.</p>
          <form @submit.prevent="resetPassword">
            <div class="mb-3">
              <input
                class="shadow form-control"
                v-model="password"
                required="required"
                type="password"
                name="password"
                placeholder="Password"
              />
            </div>

            <div class="mb-3">
              <input
                class="shadow form-control"
                v-model="passwordConfirm"
                required="required"
                type="password"
                name="confirmPassword"
                placeholder="Confirm Password"
              />
            </div>

            <div class="mb-5">
              <button class="btn btn-primary shadow" type="submit">
                Reset password
              </button>
            </div>

            <div v-if="errorMessage" class="alert alert-danger">
              <p class="mb-0"><strong>Failed!</strong> {{ errorMessage }}</p>
            </div>

            <p class="text-muted">
              Remembered your password?
              <router-link to="/login"
                >Yes
                <img
                  src="src/assets/img/arrow-right.svg"
                  alt="Arrow Right Icon"
                />
              </router-link>
            </p>
            <p class="text-muted">
              Dont have an account?
              <router-link to="/signup"
                >Sign up
                <img
                  src="src/assets/img/arrow-right.svg"
                  alt="Arrow Right Icon"
                />
              </router-link>
            </p>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped></style>
