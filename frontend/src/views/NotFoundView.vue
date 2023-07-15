<script>
export default {
  name: "NotFoundView",
};
</script>

<template>
  <section class="py-5 mt-5">
    <div class="container">
      <div
        class="row row-cols-1 d-flex justify-content-center align-items-center"
      >
        <div class="col-md-10 text-center">
          <img
            class="img-fluid w-100"
            src="src/assets/img/illustrations/404.svg"
            alt="NotFoundView"
          />
        </div>
        <div class="col text-center">
          <h2 class="display-3 fw-bold mb-4">Page Not Found</h2>
          <p class="fs-4 text-muted">It looks that you are lost again !!</p>
        </div>
      </div>
    </div>
  </section>
</template>
