<script setup>
import {ref} from 'vue';
import axiosInstance from '@/api/axiosInstance';
import {useAuthStore} from '@/stores';
import {useRouter} from 'vue-router';

const loginRequest = ref({
  email: '',
  password: ''
});
const errorMessage = ref('');
const authStore = useAuthStore();
const router = useRouter();

async function login() {
  try {
    // reset the error message
    clearErrorMessages();

    // send the login request to the server
    const response = await axiosInstance.post(
        'auth/authenticate', // the endpoint
        loginRequest.value, // the request body
        {withCredentials: true}
    );

    // get the token from the response
    const accessToken = response.data.access_token;
    const refreshToken = response.data.refresh_token;

    // set the token in local storage
    localStorage.setItem('access_token', accessToken);
    localStorage.setItem('refresh_token', refreshToken);

    // update the authorization header
    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;

    // decode the token and get the user role
    const decodedToken = JSON.parse(atob(accessToken.split('.')[1]));
    const userRole = decodedToken.role;

    // call the stores login method this will update the stores state
    authStore.login(userRole);

    // redirect to the home page
    await router.push('/');

  } catch (error) {
    if (error.response) {
      // An error response was received from the server
      showErrorMessage(error.response.data.message);
    } else if (error.request) {
      // The request was made but no response was received.
      // for example a CORS error
      showErrorMessage('Unable to connect to the server. Please try again later.');
    } else {
      // Something else went wrong
      showErrorMessage('An error occurred while processing your request.');
    }
  }
}

const clearErrorMessages = () => {
  errorMessage.value = '';
};

const showErrorMessage = (message) => {
  errorMessage.value = message;
};
</script>

<template>
  <section class="py-4 py-md-5 my-5">
    <div class="container py-md-5">
      <div class="row">
        <div class="col-md-6 text-center">
          <img class="img-fluid w-100" src="src/assets/img/illustrations/login.svg" alt="login-img">
        </div>
        <div class="col-md-5 col-xl-4 text-center text-md-start">
          <h2 class="display-6 fw-bold mb-5">
            <span class="underline pb-1">
              <strong>Login</strong>
            </span>
          </h2>
          <form @submit.prevent="login">
            <div class="mb-3">
              <input class="shadow form-control"
                     v-model="loginRequest.email"
                     required="required"
                     type="email" name="email"
                     placeholder="Email">
            </div>
            <div class="mb-3">
              <input class="shadow form-control"
                     v-model="loginRequest.password"
                     type="password"
                     name="password"
                     placeholder="Password">
            </div>
            <div class="mb-5">
              <button class="btn btn-primary shadow" type="submit">Log in</button>
            </div>
            <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
            <p class="text-muted">Dont have an account?
              <router-link to="/signup">Sign up
                <img src="src/assets/img/arrow-right.svg" alt="Arrow Right Icon">
              </router-link>
            </p>
            <p class="text-muted">Forgot your password?
              <router-link to="/forgotten-password">Yes
                <img src="src/assets/img/arrow-right.svg" alt="Arrow Right Icon">
              </router-link>
            </p>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>
