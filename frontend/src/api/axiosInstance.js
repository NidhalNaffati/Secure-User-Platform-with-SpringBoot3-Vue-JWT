import axios from "axios";

// set the base URL from the .env file
const baseURL = import.meta.env.VITE_API_URL + "/api/v1";

// create an axios instance
const axiosInstance = axios.create({
  baseURL,
});

// set the Authorization header for every request
const token = localStorage.getItem("access_token");
if (token != null) {
  axiosInstance.defaults.headers.common["Authorization"] = `Bearer ${token}`;
}

export default axiosInstance;
