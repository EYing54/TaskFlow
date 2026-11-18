import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/Login.vue";
import TaskView from "../views/TaskView.vue";

const routes = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/tasks",
    name: "Tasks",
    component: TaskView,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
