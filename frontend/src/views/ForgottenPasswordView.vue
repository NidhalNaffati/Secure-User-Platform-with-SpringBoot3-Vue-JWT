<script setup>
import { ref } from "vue";
import axiosInstance from "@/api/axiosInstance";

const email = ref("");
const errorMessage = ref("");
const successMessage = ref("");
const isLoading = ref(false);

const sendEmail = async () => {
  try {
    // clear the success & error messages
    clearMessages();
    // Enable loading indicator and disable button
    isLoading.value = true;
    // send the reset request to the server
    const response = await axiosInstance.post(
      "auth/forgot-password",
      { email: email.value },
      { withCredentials: false },
    );
    // check the response status
    if (response.status === 200) {
      isLoading.value = false;
      showSuccessMessage("Check your email for a reset link.");
    } else {
      // if the response status is not 200 or 400
      // then show an error message
      showErrorMessage(response.data.message);
    }
  } catch (e) {
    // Disable loading indicator and enable button
    isLoading.value = false;

    if (e.response) {
      // if the response status is 404 then show a success message
      if (e.response.status === 404) {
        // i m showing a success message even if the email doesn't exist
        // why ?
        // because I don't want to tell the user if the email exists or not for security reasons
        // BE SMART BE SAFE :)
        showSuccessMessage("Check your email for a reset link.");
      } else {
        // if the request failed then show an error message
        showErrorMessage(e.response.data);
      }
    } else if (e.request) {
      // The request was made but no response was received.
      // for example a CORS error
      showErrorMessage(
        "Unable to connect to the server. Please try again later.",
      );
    } else {
      // Something else went wrong
      showErrorMessage("An error occurred while processing your request.");
    }
  }
};
const clearMessages = () => {
  errorMessage.value = "";
  successMessage.value = "";
};

const showErrorMessage = (message) => {
  errorMessage.value = message;
};

const showSuccessMessage = (message) => {
  successMessage.value = message;
};
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
            Forgot your <span class="underline">password</span>?
          </h2>
          <p class="text-muted">
            Enter the email associated with your account and we'll send you a
            reset link.
          </p>
          <form @submit.prevent="sendEmail">
            <div class="mb-3">
              <input
                class="shadow form-control"
                v-model="email"
                required="required"
                type="email"
                name="email"
                placeholder="Email"
              />
            </div>
            <div class="mb-5">
              <button
                class="btn btn-primary shadow"
                type="submit"
                :disabled="isLoading"
              >
                <span
                  v-if="isLoading"
                  class="spinner-border spinner-border-sm me-2"
                  role="status"
                  aria-hidden="true"
                ></span>
                {{ isLoading ? "Sending Email..." : "Reset password" }}
              </button>
            </div>

            <div v-if="successMessage" class="alert alert-success">
              <p class="mb-0">
                <strong>Success, Email send successfully.</strong>
                <br />
                {{ successMessage }}
              </p>
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
