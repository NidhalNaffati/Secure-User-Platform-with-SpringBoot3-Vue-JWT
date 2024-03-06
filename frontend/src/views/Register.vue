<script setup>
import axiosInstance from "@/api/axiosInstance";
import router from "@/router";
import {ref} from "vue";

const registerRequest = ref({
  firstName: "",
  lastName: "",
  gender: "MALE",
  email: "",
  password: "",
  role: "ROLE_USER",
  confirmPassword: "",
});

const isLoading = ref(false);

const errorsArray = ref([]);
const errorMessage = ref("");

const validatePassword = () => {
  // Check password requirements
  const hasMinLength = registerRequest.value.password.length >= 8;
  const hasDigit = /\d/.test(registerRequest.value.password);
  const hasLowerCase = /[a-z]/.test(registerRequest.value.password);
  const hasUpperCase = /[A-Z]/.test(registerRequest.value.password);
  const passwordMatch = registerRequest.value.password === registerRequest.value.confirmPassword;

  // Return true if all requirements are met, false otherwise
  return hasMinLength && hasDigit && hasLowerCase && hasUpperCase && passwordMatch;
};

const submit = async () => {
  try {
    clearErrors();
    isLoading.value = true; // Enable loading indicator and disable button
    const response = await axiosInstance.post(
      "auth/register",
      registerRequest.value,
    );
    if (response.status === 201) await router.push("/login");
  } catch (error) {
    isLoading.value = false; // Disable loading indicator and enable button
    if (error.response) {
      // An error response was received from the server
      if (error.response.status === 422)
        errorsArray.value = error.response.data.message
          .slice(1, -1)
          .split(", ");
      else {
        showErrorMessage(error.response.data);
      }
    } else if (error.request) {
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

const showErrorMessage = (message) => {
  errorMessage.value = message;
};

const clearErrors = () => {
  errorsArray.value = [];
  errorMessage.value = "";
};
</script>

<template>
  <section class="py-4 py-md-5 my-5">
    <div class="container py-md-5">
      <div class="row">
        <div class="col-md-6 text-center">
          <img
            class="img-fluid w-100"
            src="src/assets/img/illustrations/register.svg"
            alt="register-img"
          />
          <p class="text-muted">
            Have an account?
            <router-link to="/login">
              Log in
              <img src="src/assets/img/arrow-right.svg" alt="right-arrow"/>
            </router-link>
          </p>
          <p class="text-muted">
            Forgot your password?
            <router-link to="/forgotten-password">
              Yes <img src="src/assets/img/arrow-right.svg" alt="right-arrow"/>
            </router-link>
          </p>
        </div>
        <div class="col-md-5 col-xl-4 text-center text-md-start">
          <h2 class="display-6 fw-bold mb-5">
            <span class="underline pb-1">
              <strong>Sign up</strong>
            </span>
          </h2>
          <form @submit.prevent="submit">
            <div class="mb-3">
              <input
                v-model="registerRequest.firstName"
                class="shadow-sm form-control"
                required="required"
                type="text"
                name="first-name"
                placeholder="First Name"
              />
            </div>
            <div class="mb-3">
              <input
                v-model="registerRequest.lastName"
                class="shadow-sm form-control"
                required="required"
                type="text"
                name="last-nam"
                placeholder="Last Name"
              />
            </div>
            <div class="mb-3">
              <div class="row">
                <div class="col">
                  <div class="form-check">
                    <input
                      class="form-check-input"
                      type="radio"
                      id="user-radio"
                      name="user-type"
                      value="MALE"
                      v-model="registerRequest.gender"
                      checked
                    />
                    <label class="form-check-label" for="user-radio"
                    >Male</label
                    >
                  </div>
                </div>

                <div class="col">
                  <div class="form-check">
                    <input
                      class="form-check-input"
                      type="radio"
                      id="doctor-radio"
                      name="user-type"
                      value="FEMALE"
                      v-model="registerRequest.gender"
                    />
                    <label class="form-check-label" for="doctor-radio"
                    >Female</label
                    >
                  </div>
                </div>
              </div>
            </div>
            <div class="mb-3">
              <input
                v-model="registerRequest.email"
                class="shadow-sm form-control"
                required="required"
                type="email"
                name="email"
                placeholder="Email"
              />
            </div>
            <div class="mb-3">
              <input
                v-model="registerRequest.password"
                class="shadow-sm form-control"
                required="required"
                type="password"
                name="password"
                placeholder="Password"
              />
            </div>
            <div class="mb-3">
              <input
                v-model="registerRequest.confirmPassword"
                class="shadow-sm form-control"
                required="required"
                type="password"
                name="password_repeat"
                placeholder="Repeat Password"
              />
            </div>
            <div class="mb-3">
              <!-- add a list of check points to make sure that password length is 8-->
              <ul v-if="registerRequest.password.length > 0" class="password-requirements">
                <li v-if="registerRequest.password.length < 8" class="text-danger">
                  &#x2718; Password must be at least 8 characters long
                </li>
              </ul>
              <!-- add a list of check points to make sure that password contains digits -->
              <ul v-if="registerRequest.password.length > 0" class="password-requirements">
                <li v-if="!/\d/.test(registerRequest.password)" class="text-danger">
                  &#x2718; Password must contain at least one digit
                </li>
              </ul>

              <!-- add a list of check points to make sure that password contains lower case letters -->
              <ul v-if="registerRequest.password.length > 0" class="password-requirements">
                <li v-if="!/[a-z]/.test(registerRequest.password)" class="text-danger">
                  &#x2718; Password must contain at least one lower case letter
                </li>
              </ul>

              <!-- add a list of check points to make sure that password contains upper case letters -->
              <ul v-if="registerRequest.password.length > 0" class="password-requirements">
                <li v-if="!/[A-Z]/.test(registerRequest.password)" class="text-danger">
                  &#x2718; Password must contain at least one upper case letter
                </li>
              </ul>

              <!-- add a list of check points to make sure that password and password confirm matches -->
              <ul v-if="registerRequest.password.length > 0" class="password-requirements">
                <li v-if="registerRequest.password !== registerRequest.confirmPassword" class="text-danger">
                  &#x2718; Password and confirm password must match
                </li>
              </ul>
            </div>
            <div class="mb-5">
              <button
                class="btn btn-primary shadow"
                type="submit"
                :disabled="!validatePassword() || isLoading"
              >
                <span
                  v-if="isLoading"
                  class="spinner-border spinner-border-sm me-2"
                  role="status"
                  aria-hidden="true"
                ></span>
                {{ isLoading ? "Creating account..." : "Create account" }}
              </button>
            </div>

            <div v-if="errorMessage" class="alert alert-danger">
              {{ errorMessage }}
            </div>

            <div v-if="errorsArray.length" class="alert alert-danger">
              <ul>
                <li v-for="(error, index) in errorsArray" :key="index">
                  {{ error }}
                </li>
              </ul>
            </div>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.password-requirements {
  list-style-type: none;
  padding-left: 0;
}
</style>