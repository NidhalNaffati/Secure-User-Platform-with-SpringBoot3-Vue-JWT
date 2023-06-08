import axios from 'axios'

const axiosInstance = axios.create({
    baseURL: 'http://localhost:9090/api/v1',
})

const token = localStorage.getItem('access_token')
if (token != null) {
    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

export default axiosInstance