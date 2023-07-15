import axiosInstance from "@/api/axiosInstance";
import router from "@/router";
import { useAuthStore } from "@/stores";

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    // If the error is 401 and the message is 'Access token expired', then try to refresh the access token
    if (
      error.response.status === 401 &&
      error.response.data === "Access token expired"
    ) {
      console.log("Access token expired, trying to refresh it");
      try {
        // Send a POST request to the refresh token endpoint
        console.log(
          "Sending a POST request to the refresh token endpoint",
          localStorage.getItem("refresh_token"),
        );
        const response = await axiosInstance.post(
          "/auth/refresh-token",
          // No data required
          null,
          // Send the refresh token in the Authorization header
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("refresh_token")}`,
            },
          },
        );
        console.log("response", response);

        // Update the access token in the local storage
        console.log("New access token: ", response.data.access_token);
        localStorage.setItem("access_token", response.data.access_token);

        // Retry the original request with the new access token
        const { config } = error;
        config.headers.Authorization = `Bearer ${response.data.access_token}`;
        return axiosInstance.request(config);
      } catch (refreshError) {
        //console.log('status', refreshError.response.status);
        // console.log('Error while trying to refresh the access token: ', refreshError);
        // If the refresh token is expired, then logout the user
        if (refreshError.response.status === 403) {
          console.log("Refresh token expired, logging out the user");
          // Remove tokens from local storage
          localStorage.removeItem("access_token");
          localStorage.removeItem("refresh_token");

          const authStore = useAuthStore();

          // Set role to null and isAuthenticated to false
          authStore.logout();

          // Redirect to the login page with sessionExpired query parameter
          await router.push({
            path: "/login",
            query: { sessionExpired: true },
          });
        }
      }
    }
    // Handle other errors
    return Promise.reject(error);
  },
);
