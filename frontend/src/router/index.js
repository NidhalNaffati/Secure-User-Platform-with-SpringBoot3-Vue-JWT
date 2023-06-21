import {createRouter, createWebHistory} from "vue-router";
import HomeView from "../views/HomeView.vue";
import Register from "../views/Register.vue";
import Login from "../views/Login.vue";
import NotFoundView from "../views/NotFoundView.vue";
import ForgottenPasswordView from "@/views/ForgottenPasswordView.vue";
import AdminPage from "@/views/AdminPage.vue";

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
        },
        {
            path: "/login",
            name: "login",
            component: Login,
        },
        {
            path: "/forgotten-password",
            name: "forgotten-password",
            component: ForgottenPasswordView,
        },
        {
            path: "/admin",
            name: "admin",
            component: AdminPage,
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

export default router
