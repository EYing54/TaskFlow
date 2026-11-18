<template>
  <div class="workspace">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-symbol">✓</span>
        <span>TaskFlow</span>
      </div>

      <div class="nav-group">
        <p class="nav-heading">任务</p>

        <button
          v-for="item in navigation"
          :key="item.value"
          type="button"
          class="nav-item"
          :class="{ active: currentStatus === item.value }"
          @click="currentStatus = item.value"
        >
          <span>{{ item.label }}</span>
          <span class="nav-count">
            {{ getCount(item.value) }}
          </span>
        </button>
      </div>

      <div class="sidebar-bottom">
        <span class="user-avatar">D</span>
        <span>个人工作区</span>
      </div>
    </aside>

    <main class="main-content">
      <div class="content-inner">
        <header class="page-header">
          <div>
            <h1>我的任务</h1>
            <p>管理日常事项，保持进度清晰。</p>
          </div>
          <span class="total-count"> {{ filteredTasks.length }} 项任务 </span>
        </header>

        <div class="toolbar">
          <input
            v-model="keyword"
            type="search"
            class="search-input"
            placeholder="搜索任务..."
            aria-label="搜索任务"
          />
        </div>

        <div class="list-heading">
          <span>任务</span>
          <span>状态 / 截止日期</span>
        </div>

        <div v-if="loading" class="feedback">正在加载任务...</div>

        <div v-else-if="errorMessage" class="feedback">
          <p>{{ errorMessage }}</p>
          <button type="button" @click="loadTasks">重新加载</button>
        </div>

        <div v-else-if="filteredTasks.length === 0" class="feedback">
          暂无符合条件的任务
        </div>

        <div v-else class="task-list">
          <article
            v-for="task in filteredTasks"
            :key="task.id"
            class="task-row"
          >
            <span
              class="task-indicator"
              :class="{ completed: task.status === 2 }"
              aria-hidden="true"
            >
              {{ task.status === 2 ? "✓" : "" }}
            </span>

            <div class="task-info">
              <h3 :class="{ done: task.status === 2 }">
                {{ task.title }}
              </h3>
              <p v-if="task.description">
                {{ task.description }}
              </p>

              <div class="task-meta">
                <span>{{ task.category || "未分类" }}</span>
                <span class="meta-separator">·</span>
                <span :class="'priority-' + task.priority">
                  {{ priorityLabels[task.priority] || "普通" }}
                </span>
              </div>
            </div>

            <div class="task-side">
              <span class="status-label" :class="'status-' + task.status">
                {{ statusLabels[task.status] || "未知" }}
              </span>

              <time v-if="task.dueDate">
                {{ task.dueDate }}
              </time>
              <span v-else class="no-date">无截止日期</span>
            </div>
          </article>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import request from "../utils/request";

const tasks = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const keyword = ref("");
const currentStatus = ref("all");

const navigation = [
  { label: "全部任务", value: "all" },
  { label: "待办", value: "0" },
  { label: "进行中", value: "1" },
  { label: "已完成", value: "2" },
];

const statusLabels = {
  0: "待办",
  1: "进行中",
  2: "已完成",
};

const priorityLabels = {
  1: "高优先级",
  2: "中优先级",
  3: "低优先级",
};

const getCount = (status) => {
  if (status === "all") {
    return tasks.value.length;
  }

  return tasks.value.filter((task) => String(task.status) === status).length;
};

const filteredTasks = computed(() => {
  const search = keyword.value.trim().toLowerCase();

  return tasks.value.filter((task) => {
    const matchesStatus =
      currentStatus.value === "all" ||
      String(task.status) === currentStatus.value;

    const matchesKeyword =
      !search ||
      (task.title || "").toLowerCase().includes(search) ||
      (task.description || "").toLowerCase().includes(search);

    return matchesStatus && matchesKeyword;
  });
});

const loadTasks = async () => {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await request.get("/api/tasks");
    tasks.value = response.data;
  } catch (error) {
    errorMessage.value = error.response?.data?.message || "任务加载失败";
  } finally {
    loading.value = false;
  }
};

onMounted(loadTasks);
</script>

<style scoped>
.workspace {
  display: flex;
  min-height: 100vh;
  background: #fff;
  color: #242424;
}

.sidebar {
  width: 236px;
  flex-shrink: 0;
  background: #f7f7f5;
  border-right: 1px solid #eaeae7;
  display: flex;
  flex-direction: column;
  padding: 28px 14px 18px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  font-size: 19px;
  font-weight: 650;
  letter-spacing: -0.5px;
}

.brand-symbol {
  display: grid;
  place-items: center;
  width: 29px;
  height: 29px;
  border-radius: 8px;
  background: #397c65;
  color: white;
  font-size: 18px;
}

.nav-group {
  margin-top: 48px;
}

.nav-heading {
  padding: 0 12px;
  margin: 0 0 12px;
  color: #999;
  font-size: 12px;
  font-weight: 600;
}

.nav-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 11px 12px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #666;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
}

.nav-item:hover {
  background: #ededeb;
}

.nav-item.active {
  background: #e6ebe5;
  color: #32644e;
  font-weight: 600;
}

.nav-count {
  color: #999;
  font-size: 12px;
  font-weight: 400;
}

.sidebar-bottom {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: auto;
  padding: 16px 12px 4px;
  border-top: 1px solid #e8e8e5;
  color: #777;
  font-size: 13px;
}

.user-avatar {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #e3e5e1;
  color: #555;
  font-weight: 600;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.content-inner {
  max-width: 980px;
  margin: 0 auto;
  padding: 64px 48px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 36px;
}

.page-header h1 {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 650;
  letter-spacing: -0.8px;
  color: #222;
}

.page-header p {
  margin: 0;
  font-size: 13px;
  color: #999;
}

.total-count {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
}

.toolbar {
  margin-bottom: 28px;
}

.search-input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fafafa;
  color: #333;
  outline: none;
  font-size: 14px;
}

.search-input:focus {
  border-color: #7ca795;
  background: white;
}

.list-heading {
  display: flex;
  justify-content: space-between;
  padding: 0 0 14px;
  border-bottom: 1px solid #eaeaea;
  color: #999;
  font-size: 12px;
}

.task-row {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 22px 4px;
  border-bottom: 1px solid #efefef;
}

.task-indicator {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  margin-top: 2px;
  border: 1.5px solid #c6c9c6;
  border-radius: 50%;
  color: white;
  font-size: 12px;
}

.task-indicator.completed {
  background: #46866d;
  border-color: #46866d;
}

.task-info {
  flex: 1;
  min-width: 0;
}

.task-info h3 {
  margin: 0 0 6px;
  color: #303030;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
}

.task-info h3.done {
  color: #aaa;
  text-decoration: line-through;
}

.task-info p {
  margin: 0 0 10px;
  color: #898989;
  font-size: 13px;
  line-height: 1.6;
}

.task-meta {
  display: flex;
  gap: 9px;
  font-size: 12px;
  color: #999;
}

.meta-separator {
  color: #ccc;
}

.priority-1 {
  color: #c66b64;
}

.priority-2 {
  color: #b18b4b;
}

.priority-3 {
  color: #999;
}

.task-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  min-width: 100px;
}

.status-label {
  font-size: 12px;
}

.status-0 {
  color: #a08b5b;
}

.status-1 {
  color: #587eab;
}

.status-2 {
  color: #48866c;
}

.task-side time,
.no-date {
  font-size: 12px;
  color: #aaa;
}

.feedback {
  padding: 60px 15px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.feedback button {
  padding: 8px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
}

@media (max-width: 720px) {
  .workspace {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    padding: 14px;
    border-right: 0;
    border-bottom: 1px solid #eee;
  }

  .nav-group {
    margin-top: 18px;
  }

  .nav-item {
    padding: 9px 12px;
  }

  .sidebar-bottom {
    display: none;
  }

  .content-inner {
    padding: 32px 20px;
  }

  .task-side {
    min-width: 75px;
  }

  .page-header h1 {
    font-size: 24px;
  }
}
</style>
