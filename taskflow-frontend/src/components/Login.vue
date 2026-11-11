<template>
  <div>
    <el-card>
      <h2 v-if="avaliableLogon">登录</h2>
      <h2 v-else>注册</h2>
      <input type="text" placeholder="请输入用户名" v-model="username" />
      <input type="password" placeholder="请输入密码" v-model="password" />
      <el-button v-model="login">登录</el-button>
      <el-button>注册</el-button>
    </el-card>
  </div>
</template>

<script setup>
const avaliableLogon = ref(false);
const username = ref("");
const password = ref();

const login = () => {};
</script>
