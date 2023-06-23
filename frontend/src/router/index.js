import {createRouter, createWebHistory} from "vue-router";
import HomeView from "../views/HomeView.vue";
import Register from "../views/Register.vue";
import Login from "../views/Login.vue";
import NotFoundView from "../views/NotFoundView.vue";
import ForgottenPasswordView from "@/views/ForgottenPasswordView.vue";
import AdminPage from "@/views/AdminPage.vue";
import {useAuthStore} from "@/stores";

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
            path: "/admin",
            name: "admin",
            component: AdminPage,
            beforeEnter: adminGuard,
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
    if (authStore.isUserAuthenticated) { // If the user is already authenticated
        next({name: "home"}); // Redirect to home page
    } else { // Otherwise
        next(); // Proceed to the requested page
    }
}

function adminGuard(to, from, next) {
    const authStore = useAuthStore();
    if (authStore.isUserAuthenticated && authStore.isAdmin) {
        next();
    } else {
        next({ name: "home" }); // Redirect to home page
        console.error("You are not authorized to access this page.");
        /*
        * TODO : add a message to the user to tell him that he is not authorized to access this page.
        *  WE CAN USE THE TOAST COMPONENT FOR THIS
        * 
        * */
    }
}

export default router
