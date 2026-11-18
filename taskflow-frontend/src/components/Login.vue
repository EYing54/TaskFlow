<template>
  <div class="login-container">
    <form class="login-card" @submit.prevent="login">
      <h2>TaskFlow</h2>
      <p>用户登录</p>

      <input
        v-model="username"
        type="text"
        placeholder="用户名"
        autocomplete="username"
      />

      <input
        v-model="password"
        type="password"
        placeholder="密码"
        autocomplete="current-password"
      />

      <button type="submit" :disabled="loading">
        {{ loading ? "登录中..." : "登录" }}
      </button>

      <p v-if="message" class="message">
        {{ message }}
      </p>
    </form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { userLogin } from "../api/user";

const router = useRouter();

const username = ref("");
const password = ref("");
const message = ref("");
const loading = ref(false);

const login = async () => {
  message.value = "";

  if (!username.value.trim() || !password.value) {
    message.value = "请输入用户名和密码";
    return;
  }

  loading.value = true;

  try {
    await userLogin(username.value.trim(), password.value);

    await router.push("/tasks");
  } catch (error) {
    message.value = error.response?.data?.message || "登录失败，请检查后端服务";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 320px;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  border: 1px solid #ddd;
  border-radius: 12px;
  background: white;
}

input,
button {
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
}

button {
  background: #409eff;
  color: white;
  border: none;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message {
  text-align: center;
}
</style>
