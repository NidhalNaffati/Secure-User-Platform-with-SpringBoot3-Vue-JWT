import {useAuthStore} from "@/stores";

/**
 This file contains the guards used in the router.
 The guards are used to protect the routes from unauthorized access.
 The guards are used to check if the user is authenticated, admin or user.
 **/

function redirectIfAuthenticated(to, from, next) {
  const authStore = useAuthStore();

  if (authStore.isUserAuthenticated) {
    // If the user is already authenticated
    console.warn("You are already authenticated.");
    next({name: "home"}); // Redirect to home page
  } else {
    // Otherwise
    next(); // Proceed to the requested page
  }
}

// check if the user is authenticated
// the role does not matter
function authenticatedGuard(to, from, next) {
  // create store instance
  const authStore = useAuthStore();

  const isUserAuthenticated = authStore.isUserAuthenticated;
  if (!isUserAuthenticated) {
    console.warn("You are not authenticated.");
    // redirect to login page
    next({name: "login"});
  }
  // Proceed to the requested page
  else next();
}


// role guard used to check if the user is authenticated, and he is an admin or user
function roleGuard(to, from, next, role) {

  const authStore = useAuthStore();

  const isUserAuthenticated = authStore.isUserAuthenticated;

  if (role === 'admin') {
    // if the user is authenticated and is admin
    const isAdmin = authStore.isAdmin;
    if (isUserAuthenticated && isAdmin)
      next(); // Proceed to the requested page if the user is authenticated and is admin
    else
      next({name: "home"});  // redirect to home page
  }
  if (role === 'user') {
    // if the user is authenticated and is user
    const isUser = authStore.isUser;
    if (isUserAuthenticated && isUser)
      next(); // Proceed to the requested page if the user is authenticated and is user
    else
      next({name: "home"}); // redirect to home page
  }
}

function adminGuard(to, from, next) {
  roleGuard(to, from, next, 'admin');
}

function userGuard(to, from, next) {
  roleGuard(to, from, next, 'user');
}

export {redirectIfAuthenticated, authenticatedGuard, adminGuard, userGuard};