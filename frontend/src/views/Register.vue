<script setup>
import axiosInstance from "@/api/axiosInstance";
import router from "@/router";
import {ref} from "vue";

const registerRequest = ref({
  firstName: '',
  lastName: '',
  gender: 'MALE',
  email: '',
  password: '',
  role: 'ROLE_USER',
  confirmPassword: '',
});
const errorsArray = ref([]);
const errorMessage = ref('');

const submit = async () => {
  try {
    clearErrors();
    const response = await axiosInstance.post('auth/register', registerRequest.value);
    if (response.status === 201)
      await router.push('/login');
  } catch (error) {
    if (error.response) {
      // An error response was received from the server
      if (error.response.status === 422)
        errorsArray.value = error.response.data.message.slice(1, -1).split(', ');
      else{
        showErrorMessage(error.response.data);
      }
    } else if (error.request) {
      // The request was made but no response was received.
      // for example a CORS error
      showErrorMessage('Unable to connect to the server. Please try again later.');
    } else {
      // Something else went wrong
      showErrorMessage('An error occurred while processing your request.');
    }
  }
};

const showErrorMessage = (message) => {
  errorMessage.value = message;
};

const clearErrors = () => {
  errorsArray.value = [];
  errorMessage.value = '';
};
</script>

<template>
  <section class="py-4 py-md-5 my-5">
    <div class="container py-md-5">
      <div class="row">
        <div class="col-md-6 text-center">
          <img class="img-fluid w-100" src="src/assets/img/illustrations/register.svg" alt="register-img">
          <p class="text-muted">Have an account?
            <router-link to="/login">
              Log in <img src="src/assets/img/arrow-right.svg" alt="right-arrow">
            </router-link>
          </p>
          <p class="text-muted">Forgot your password?
            <router-link to="/forgotten-password">
              Yes <img src="src/assets/img/arrow-right.svg" alt="right-arrow">
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
              <input v-model="registerRequest.firstName"
                     class="shadow-sm form-control"
                     required="required"
                     type="text" name="first-name"
                     placeholder="First Name">
            </div>
            <div class="mb-3">
              <input v-model="registerRequest.lastName"
                     class="shadow-sm form-control"
                     required="required"
                     type="text" name="last-nam"
                     placeholder="Last Name">
            </div>
            <div class="mb-3">
              <div class="row">
                <div class="col">
                  <div class="form-check">
                    <input class="form-check-input" type="radio"
                           id="user-radio" name="user-type"
                           value="MALE" v-model="registerRequest.gender" checked>
                    <label class="form-check-label" for="user-radio">Male</label>
                  </div>
                </div>

                <div class="col">
                  <div class="form-check">
                    <input class="form-check-input" type="radio"
                           id="doctor-radio" name="user-type"
                           value="FEMALE" v-model="registerRequest.gender">
                    <label class="form-check-label" for="doctor-radio">Female</label>
                  </div>
                </div>
              </div>
            </div>
            <div class="mb-3">
              <input v-model="registerRequest.email"
                     class="shadow-sm form-control"
                     required="required"
                     type="email" name="email"
                     placeholder="Email">
            </div>
            <div class="mb-3">
              <input v-model="registerRequest.password"
                     class="shadow-sm form-control"
                     required="required"
                     type="password" name="password"
                     placeholder="Password">
            </div>
            <div class="mb-3">
              <input v-model="registerRequest.confirmPassword"
                     class="shadow-sm form-control"
                     required="required"
                     type="password" name="password_repeat"
                     placeholder="Repeat Password">
            </div>
            <div class="mb-5">
              <button class="btn btn-primary shadow" type="submit">Create account</button>
            </div>

            <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

            <div v-if="errorsArray.length" class="alert alert-danger">
              <ul>
                <li v-for="(error, index) in errorsArray" :key="index">{{ error }}</li>
              </ul>
            </div>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>

