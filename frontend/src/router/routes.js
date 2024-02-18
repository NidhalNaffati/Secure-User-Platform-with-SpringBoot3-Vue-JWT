import HomeView from "@/views/HomeView.vue";
import Register from "@/views/Register.vue";
import Login from "@/views/Login.vue";
import NotFoundView from "@/views/NotFoundView.vue";
import ForgottenPasswordView from "@/views/ForgottenPasswordView.vue";
import AdminPage from "@/views/AdminPage.vue";
import UserPage from "@/views/UserPage.vue";
import ResetPasswordPage from "@/views/ResetPasswordPage.vue";
import AuthenticatedUserPage from "@/views/AuthenticatedUserPage.vue";

import {redirectIfAuthenticated, authenticatedGuard, adminGuard, userGuard} from "@/router/guards";


const routes =
  [
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
  ];

export default routes;
