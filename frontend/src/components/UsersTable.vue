<template>
  <section class="py-4 py-md-5 my-5">
    <div class="container-md py-5">
      <div class="row mb-2 mb-5">
        <div class="col text-center">
          <button class="btn btn-outline-secondary" @click="getLockedUsers">
            Locked Users
          </button>
        </div>
        <div class="col text-center">
          <button class="btn btn-outline-secondary" @click="getAllUsers">
            All Users
          </button>
        </div>
        <div class="col text-center">
          <button class="btn btn-outline-secondary" @click="getUnlockedUsers">
            Unlocked Users
          </button>
        </div>
      </div>
      <div v-if="errorMessage" class="alert alert-danger">
        <strong>Something went wrong!</strong>
        {{ errorMessage }}
      </div>
      <div class="row">
        <!--Users table-->
        <vue-good-table
          :columns="columns"
          :rows="users"
          :search-options="{enabled: true}"
          :pagination-options="{enabled: true}"
          styleClass="vgt-table striped"
        >
          <template v-slot:table-row="props">
            <span v-if="props.column.field === 'action'">
                <!-- delete button -->
                <button
                  class="btn btn-outline-danger mx-1"
                  data-bs-toggle="modal" data-bs-target="#modal-1"
                  @click="setUserToDelete(props.row.email, props.row.originalIndex)"
                >
                  <svg
                    class="icon icon-tabler icon-tabler-trash fs-3"
                    xmlns="http://www.w3.org/2000/svg"
                    width="1em"
                    height="1em"
                    viewBox="0 0 24 24"
                    stroke-width="2"
                    stroke="currentColor"
                    fill="none"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                    <line x1="4" y1="7" x2="20" y2="7"/>
                    <line x1="10" y1="11" x2="10" y2="17"/>
                    <line x1="14" y1="11" x2="14" y2="17"/>
                    <path d="M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12"/>
                    <path d="M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3"/>
                  </svg>
                </button>
              <!-- lock button -->
                <button
                  class="btn btn-outline-primary mx-1"
                  v-if=props.row.accountNonLocked
                  @click="lockUserAccount(props.row.email, props.row.originalIndex)"
                >
                  UNLOCKED.. Click to lock
                </button>
              <!--unlock button-->
                <button
                  class="btn btn-outline-primary mx-1"
                  v-else
                  @click="unlockUserAccount(props.row.email, props.row.originalIndex)"
                >
                  LOCKED.. Click to unlock
                </button>
            </span>
          </template>
        </vue-good-table>

        <!--Start Modal-->
        <div class="modal fade" role="dialog" tabindex="-1" id="modal-1">
          <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
              <div class="modal-body" id="modal-body">
                <div class="row d-flex justify-content-center">
                  <h3>Are you sure about that !!</h3>
                </div>
              </div>
              <div class="modal-footer">
                <button class="btn btn-light" type="button" data-bs-dismiss="modal">Close</button>
                <button class="btn btn-primary" type="button" data-bs-dismiss="modal"
                        @click="deleteUserAccount(selectedUserEmail,selectedUserIndex)">Delete
                </button>
              </div>
            </div>
          </div>
        </div>
        <!--End Modal-->
      </div>
    </div>
  </section>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import axiosInstance from "@/api/axiosInstance";
import {VueGoodTable} from "vue-good-table-next";
import 'vue-good-table-next/dist/vue-good-table-next.css';

const users = ref([]);
const columns = [
  {label: 'first name', field: 'firstName'},
  {label: 'last name', field: 'lastName'},
  {label: 'Email', field: 'email'},
  {label: 'Action', field: 'action'}
];
let selectedUserEmail = null;
let selectedUserIndex = null;
let errorMessage = '';

onMounted(() => {
  getAllUsers();
});

const getAllUsers = async () => {
  errorMessage = '';
  try {
    const response = await axiosInstance.get("admin/users");
    if (response.status === 200) {
      users.value = response.data;
      console.log(response.data)
    } else {
      errorMessage = 'Cannot get users from the server';
      users.value = [];
    }
  } catch (error) {
    errorMessage = 'Cannot get users from the server';
    users.value = [];
  }
};

const getUnlockedUsers = async () => {
  errorMessage = '';
  try {
    const response = await axiosInstance.get("admin/unlocked-users");
    if (response.status === 200) {
      console.log(response.data);
      users.value = response.data;
    } else {
      console.log(response);
      users.value = [];
    }
  } catch (error) {
    users.value = [];
    errorMessage = 'Cannot get users from the server';
  }
};

const getLockedUsers = async () => {
  errorMessage = '';
  try {
    const response = await axiosInstance.get("admin/locked-users");
    if (response.status === 200) {
      users.value = response.data;
    } else {
      errorMessage = 'Cannot get users from the server';
      users.value = [];
    }
  } catch (error) {
    users.value = [];
    errorMessage = 'Cannot get users from the server';
  }
};

const deleteUserAccount = async (email, index) => {
  errorMessage = '';
  try {
    const response = await axiosInstance.delete("admin/users", {data: {email: email}});
    if (response.status === 200) {
      users.value.splice(index, 1);
    } else {
      errorMessage = 'Cannot delete this account';
    }
  } catch (error) {
    errorMessage = 'Cannot delete this account';
    console.log(error);
  }
};

const unlockUserAccount = async (email, index) => {
  errorMessage = '';
  try {
    const url = `admin/unlock-user/${email}`;
    const response = await axiosInstance.post(url);
    if (response.status === 200) {
      users.value.splice(index, 1);
    } else {
      errorMessage = 'Cannot unlock this account';
    }
  } catch (error) {
    errorMessage = 'Cannot unlock this account';
    console.log(error);
  }
};

const lockUserAccount = async (email, index) => {
  errorMessage = '';
  try {
    const url = `admin/lock-user/${email}`;
    const response = await axiosInstance.post(url);
    if (response.status === 200) {
      users.value.splice(index, 1);
    } else {
      errorMessage = 'Cannot lock this account';
    }
  } catch (error) {
    errorMessage = 'Cannot lock this account';
    console.log(error);
  }
};

const setUserToDelete = (email, index) => {
  selectedUserEmail = email;
  selectedUserIndex = index;
};

</script>
