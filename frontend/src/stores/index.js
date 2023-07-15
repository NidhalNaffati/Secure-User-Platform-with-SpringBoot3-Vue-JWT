import { defineStore } from "pinia";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isAuthenticated: false,
    userRole: null,
  }),
  persist: {
    enabled: true,
    strategies: [
      {
        key: "user",
        storage: localStorage,
      },
    ],
  },
  getters: {
    isUserAuthenticated: (state) => state.isAuthenticated,
    isAdmin: (state) => state.userRole === "ROLE_ADMIN",
    isUser: (state) => state.userRole === "ROLE_USER",
  },
  actions: {
    login(userRole) {
      this.isAuthenticated = true;
      this.userRole = userRole;
    },
    logout() {
      this.isAuthenticated = false;
      this.userRole = null;
    },
  },
});
