<script>
import {ref} from 'vue';
import axiosInstance from '@/api/axiosInstance';

export default {
  name: 'ForgottenPasswordView',
  setup() {
    const email = ref('');
    const showErrorMessage = ref(false);
    const showSuccessMessage = ref(false);

    const sendEmail = async () => {
      try {
        showSuccessMessage.value = true;
        // send the email to the server
        const response = await axiosInstance.post(
            'auth/reset-password',
            {email: email.value},
            {withCredentials: false})
        // check the response status
        if (response.status === 200 || response.status === 400) {
          // i m showing a success message for both cases
          // why ?
          // because I don't want to tell the user if the email exists or not for security reasons
          // BE SMART BE SAFE :)
          showSuccessMessage.value = true;
        } else {
          // if the response status is not 200 or 400
          // then show an error message
          showErrorMessage.value = true;
        }
      } catch (error) {
        // if the request failed
        // then show an error message
        showErrorMessage.value = true;
      }
    };

    return {
      email,
      errorMessage: showErrorMessage,
      successMessage: showSuccessMessage,
      sendEmail,
    };
  },
};
</script>

<template>
  <section class="py-4 py-md-5 mt-5">
    <div class="container py-md-5">
      <div class="row d-flex align-items-center">
        <div class="col-md-6 text-center">
          <img class="img-fluid w-100" src="src/assets/img/illustrations/desk.svg" alt="forgotten-password-img">
        </div>
        <div class="col-md-5 col-xl-4 text-center text-md-start">
          <h2 class="display-6 fw-bold mb-4">Forgot your
            <span class="underline">password</span>?
          </h2>
          <p class="text-muted">
            Enter the email associated with your account and we'll send you a reset link.
          </p>
          <form @submit.prevent="sendEmail">
            <div class="mb-3">
              <input class="shadow form-control"
                     v-model="email"
                     required="required"
                     type="email" name="email"
                     placeholder="Email">
            </div>
            <div class="mb-5">
              <button class="btn btn-primary shadow" type="submit">Reset password</button>
            </div>
            <div v-if="successMessage" class="alert alert-success">
              <p class="mb-0">
                <strong>Success!</strong> Check your email for a reset link.
              </p>
            </div>
            <!-- this div is broken at the moment-->
            <div v-if="errorMessage" class="alert alert-danger">
              <p class="mb-0">
                <strong>Failed!</strong> Can't send reset link.
              </p>
            </div>
            <p class="text-muted">Remembered your password?
              <router-link to="/login">Yes
                <img src="src/assets/img/arrow-right.svg" alt="Arrow Right Icon">
              </router-link>
            </p>
            <p class="text-muted">Dont have an account?
              <router-link to="/signup">Sign up
                <img src="src/assets/img/arrow-right.svg" alt="Arrow Right Icon">
              </router-link>
            </p>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>
