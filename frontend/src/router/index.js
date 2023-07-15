import { createRouter, createWebHistory } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import Register from "@/views/Register.vue";
import Login from "@/views/Login.vue";
import NotFoundView from "@/views/NotFoundView.vue";
import ForgottenPasswordView from "@/views/ForgottenPasswordView.vue";
import AdminPage from "@/views/AdminPage.vue";
import { useAuthStore } from "@/stores";
import UserPage from "@/views/UserPage.vue";
import ResetPasswordPage from "@/views/ResetPasswordPage.vue";
import AuthenticatedUserPage from "@/views/AuthenticatedUserPage.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: HomeView,
    },
    {
      path: "/signup",
      name: "signup",
      component: Register,
      beforeEnter: redirectIfAuthenticated,
    },
    {
      path: "/login",
      name: "login",
      component: Login,
      beforeEnter: redirectIfAuthenticated,
    },
    {
      path: "/forgotten-password",
      name: "forgotten-password",
      component: ForgottenPasswordView,
      beforeEnter: redirectIfAuthenticated,
    },
    {
      path: "/reset-password",
      name: "reset-password",
      component: ResetPasswordPage,
      beforeEnter: redirectIfAuthenticated,
    },
    {
      path: "/authenticated",
      name: "authenticated",
      component: AuthenticatedUserPage,
      beforeEnter: authenticatedGuard,
    },
    {
      path: "/admin",
      name: "admin",
      component: AdminPage,
      beforeEnter: adminGuard,
    },
    {
      path: "/user",
      name: "userPage",
      component: UserPage,
      beforeEnter: userGuard,
    },
    {
      path: "/404",
      name: "not-found",
      component: NotFoundView,
    },
    // catch all 404
    {
      path: "/:pathMatch(.*)*",
      redirect: "/404",
    },
  ],
});

// this function is used to prevent the user from accessing the login, signup and ForgottenPassword pages when he is already authenticated.
function redirectIfAuthenticated(to, from, next) {
  const authStore = useAuthStore();
  if (authStore.isUserAuthenticated) {
    // If the user is already authenticated
    console.warn("You are already authenticated.");
    next({ name: "home" }); // Redirect to home page
  } else {
    // Otherwise
    next(); // Proceed to the requested page
  }
}

function authenticatedGuard(to, from, next) {
  // create store instance
  const authStore = useAuthStore();

  const isUserAuthenticated = authStore.isUserAuthenticated;
  if (!isUserAuthenticated) {
    console.warn("You are not authenticated.");
    // redirect to login page
    next({ name: "login" });
  }
  // Proceed to the requested page
  else next();
}

function adminGuard(to, from, next) {
  // create store instance
  const authStore = useAuthStore();

  const isUserAuthenticated = authStore.isUserAuthenticated;
  console.warn(
    "authStore.isUserAuthenticated : ",
    authStore.isUserAuthenticated,
  );
  const isAdmin = authStore.isAdmin;
  console.warn("authStore.isAdmin : ", authStore.isAdmin);

  // If the user is authenticated and is an admin
  const isUserAuthenticatedAndAuthorized = isUserAuthenticated && isAdmin;
  const isUserAuthenticatedAndNotAuthorized = isUserAuthenticated && !isAdmin;

  if (!isUserAuthenticated) {
    console.warn("You are not authenticated.");
    // redirect to login page
    next({ name: "login" });
  }
  if (isUserAuthenticatedAndAuthorized)
    // Proceed to the requested page
    next();
  if (isUserAuthenticatedAndNotAuthorized) {
    // Redirect to home page
    next({ name: "home" });
    console.warn("You are not authorized to access this page.");
    /*
     * TODO : add a message to the user to tell him that he is not authorized to access this page.
     *  WE CAN USE THE TOAST COMPONENT FOR THIS
     *
     * */
  }
}

function userGuard(to, from, next) {
  // create store instance
  const authStore = useAuthStore();

  const isUserAuthenticated = authStore.isUserAuthenticated;
  console.warn(
    "authStore.isUserAuthenticated : ",
    authStore.isUserAuthenticated,
  );
  const isUser = authStore.isUser;
  console.warn("authStore.isUser : ", authStore.isUser);

  // If the user is authenticated and is an admin
  const isUserAuthenticatedAndAuthorized = isUserAuthenticated && isUser;
  const isUserAuthenticatedAndNotAuthorized = isUserAuthenticated && !isUser;

  if (!isUserAuthenticated) {
    console.warn("You are not authenticated.");
    // redirect to login page
    next({ name: "login" });
  }
  if (isUserAuthenticatedAndAuthorized)
    // Proceed to the requested page
    next();
  if (isUserAuthenticatedAndNotAuthorized) {
    // Redirect to home page
    next({ name: "home" });
    console.warn("You are not authorized to access this page.");
    /*
     * TODO : add a message to the user to tell him that he is not authorized to access this page.
     *  WE CAN USE THE TOAST COMPONENT FOR THIS
     *
     * */
  }
}

export default router;
