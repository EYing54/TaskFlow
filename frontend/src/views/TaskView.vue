<template>
  <div class="workspace">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
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
          <span class="nav-count">{{ getCount(item.value) }}</span>
        </button>
      </div>
    </aside>

    <!-- 右侧主要内容 -->
    <main class="main-content">
      <div class="content-inner">
        <!-- 页面标题 -->
        <header class="page-header">
          <div>
            <h1>我的任务</h1>
            <p>管理日常事项，保持进度清晰。</p>
          </div>

          <span class="total-count"> {{ filteredTasks.length }} 项任务 </span>
        </header>

        <!-- 搜索与新建任务 -->
        <div class="toolbar">
          <input
            v-model="keyword"
            type="search"
            class="search-input"
            placeholder="搜索任务..."
            aria-label="搜索任务"
          />

          <button
            type="button"
            class="create-button"
            @click="showCreate = !showCreate"
          >
            {{ showCreate ? "收起表单" : "+ 新建任务" }}
          </button>
        </div>

        <!-- 新建任务表单 -->
        <form
          v-if="showCreate"
          class="create-form"
          @submit.prevent="createTask"
        >
          <h2>创建任务</h2>

          <div class="form-group">
            <label for="task-title">任务标题</label>
            <input
              id="task-title"
              v-model="newTask.title"
              type="text"
              placeholder="输入任务标题"
              maxlength="100"
              required
            />
          </div>

          <div class="form-group">
            <label for="task-description">任务描述</label>
            <textarea
              id="task-description"
              v-model="newTask.description"
              placeholder="添加任务描述（可选）"
              rows="3"
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="task-priority">优先级</label>

              <select id="task-priority" v-model.number="newTask.priority">
                <option :value="1">高优先级</option>
                <option :value="2">中优先级</option>
                <option :value="3">低优先级</option>
              </select>
            </div>

            <div class="form-group">
              <label for="task-category">分类</label>
              <input
                id="task-category"
                v-model="newTask.category"
                type="text"
                placeholder="例如：学习"
                maxlength="30"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="task-date">截止日期</label>
            <input id="task-date" v-model="newTask.dueDate" type="date" />
          </div>

          <p v-if="createError" class="form-error" role="alert">
            {{ createError }}
          </p>

          <div class="form-actions">
            <button
              type="button"
              class="cancel-button"
              :disabled="creating"
              @click="showCreate = false"
            >
              取消
            </button>

            <button type="submit" class="submit-button" :disabled="creating">
              {{ creating ? "创建中..." : "确认创建" }}
            </button>
          </div>
        </form>

        <!-- 状态修改错误提示 -->
        <p v-if="updateError" class="update-error" role="alert">
          {{ updateError }}
        </p>

        <!-- 任务列表 -->
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
            <!-- 任务完成按钮 -->
            <button
              type="button"
              class="task-indicator"
              :class="{ completed: task.status === 2 }"
              :disabled="updatingTaskId !== null"
              :aria-label="task.status === 2 ? '恢复待办' : '完成任务'"
              @click="updateTaskStatus(task.id, task.status === 2 ? 0 : 2)"
            >
              {{ task.status === 2 ? "✓" : "" }}
            </button>

            <!-- 任务信息 -->
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

            <!-- 任务状态与截止日期 -->
            <div class="task-side">
              <select
                class="status-select"
                :class="'status-' + task.status"
                :value="task.status"
                :disabled="updatingTaskId !== null"
                aria-label="任务状态"
                @change="updateTaskStatus(task.id, Number($event.target.value))"
              >
                <option
                  v-for="(label, value) in statusLabels"
                  :key="value"
                  :value="value"
                >
                  {{ label }}
                </option>
              </select>

              <time v-if="task.dueDate">
                {{ task.dueDate }}
              </time>

              <span v-else class="no-date"> 无截止日期 </span>
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

// 任务列表状态
const tasks = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const keyword = ref("");
const currentStatus = ref("all");

// 任务状态修改
const updatingTaskId = ref(null);
const updateError = ref("");

// 新建任务状态
const showCreate = ref(false);
const creating = ref(false);
const createError = ref("");

const newTask = ref({
  title: "",
  description: "",
  priority: 2,
  category: "学习",
  dueDate: "",
});

// 导航配置
const navigation = [
  { label: "全部任务", value: "all" },
  { label: "待办", value: "0" },
  { label: "进行中", value: "1" },
  { label: "已完成", value: "2" },
];

// 任务状态
const statusLabels = {
  0: "待办",
  1: "进行中",
  2: "已完成",
};

// 任务优先级
const priorityLabels = {
  1: "高优先级",
  2: "中优先级",
  3: "低优先级",
};

// 统计各状态任务数量
const getCount = (status) => {
  if (status === "all") {
    return tasks.value.length;
  }

  return tasks.value.filter((task) => String(task.status) === status).length;
};

// 根据状态和关键词筛选任务
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

// 查询任务列表
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

// 创建任务
const createTask = async () => {
  createError.value = "";

  if (!newTask.value.title.trim()) {
    createError.value = "任务标题不能为空";
    return;
  }

  creating.value = true;

  try {
    await request.post("/api/tasks", {
      title: newTask.value.title.trim(),
      description: newTask.value.description,
      priority: newTask.value.priority,
      category: newTask.value.category.trim() || "学习",
      dueDate: newTask.value.dueDate || null,
    });

    newTask.value = {
      title: "",
      description: "",
      priority: 2,
      category: "学习",
      dueDate: "",
    };

    showCreate.value = false;
    await loadTasks();
  } catch (error) {
    createError.value = error.response?.data?.message || "创建任务失败";
  } finally {
    creating.value = false;
  }
};

// 修改任务状态
const updateTaskStatus = async (taskId, status) => {
  if (updatingTaskId.value !== null) {
    return;
  }

  const task = tasks.value.find((item) => item.id === taskId);

  if (!task || task.status === status) {
    return;
  }

  updateError.value = "";
  updatingTaskId.value = taskId;

  try {
    await request.patch(`/api/tasks/${taskId}/status`, {
      status,
    });

    const currentTask = tasks.value.find((item) => item.id === taskId);

    if (currentTask) {
      currentTask.status = status;
    }
  } catch (error) {
    updateError.value = error.response?.data?.message || "任务状态修改失败";

    await loadTasks();
  } finally {
    updatingTaskId.value = null;
  }
};

// 页面加载后查询任务
onMounted(loadTasks);
</script>

<style scoped>
/* 整体布局 */
.workspace {
  display: flex;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background: #fff;
  color: #242424;
}

/* 左侧导航栏 */
.sidebar {
  width: 236px;
  height: 100%;
  box-sizing: border-box;
  flex-shrink: 0;
  padding: 32px 14px;
  background: #f7f7f5;
  border-right: 1px solid #eaeae7;
  overflow-y: auto;
}

.nav-heading {
  margin: 0 0 16px;
  padding: 0 12px;
  color: #999;
  font-size: 12px;
  font-weight: 600;
}

.nav-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 12px;
  border: none;
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

/* 右侧滚动区域 */
.main-content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.content-inner {
  max-width: 980px;
  margin: 0 auto;
  padding: 64px 48px 100px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 36px;
}

.page-header h1 {
  margin: 0 0 10px;
  color: #222;
  font-size: 28px;
  font-weight: 650;
  letter-spacing: -0.8px;
}

.page-header p {
  margin: 0;
  color: #999;
  font-size: 13px;
}

.total-count {
  color: #999;
  font-size: 13px;
  white-space: nowrap;
}

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.search-input {
  flex: 1;
  min-width: 0;
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
  background: #fff;
}

.create-button {
  padding: 12px 18px;
  border: none;
  border-radius: 8px;
  background: #397c65;
  color: #fff;
  font-size: 14px;
  white-space: nowrap;
  cursor: pointer;
}

.create-button:hover {
  background: #306b57;
}

/* 新建任务表单 */
.create-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 30px;
  padding: 24px;
  border: 1px solid #eaeaea;
  border-radius: 10px;
  background: #fff;
}

.create-form h2 {
  margin: 0 0 4px;
  color: #303030;
  font-size: 17px;
  font-weight: 600;
}

.form-group {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.form-group label {
  color: #555;
  font-size: 13px;
  font-weight: 500;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  border: 1px solid #e2e2e2;
  border-radius: 7px;
  background: #fff;
  color: #333;
  font: inherit;
  font-size: 13px;
  outline: none;
}

.form-group textarea {
  resize: vertical;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  border-color: #7ca795;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-error {
  margin: 0;
  color: #c66b64;
  font-size: 13px;
}

.update-error {
  margin: 0 0 16px;
  color: #c66b64;
  font-size: 13px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-button,
.submit-button {
  padding: 10px 18px;
  border-radius: 7px;
  font-size: 13px;
  cursor: pointer;
}

.cancel-button {
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
}

.submit-button {
  border: 1px solid #397c65;
  background: #397c65;
  color: #fff;
}

.submit-button:hover {
  background: #306b57;
}

.form-actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 任务列表 */
.list-heading {
  display: flex;
  justify-content: space-between;
  padding-bottom: 14px;
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

/* 任务完成按钮 */
.task-indicator {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  box-sizing: border-box;
  margin-top: 2px;
  padding: 0;
  border: 1.5px solid #c6c9c6;
  border-radius: 50%;
  background: transparent;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
}

.task-indicator:hover {
  border-color: #46866d;
}

.task-indicator.completed {
  background: #46866d;
  border-color: #46866d;
}

.task-indicator:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 任务信息 */
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

/* 任务状态与截止日期 */
.task-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  min-width: 100px;
}

.status-select {
  padding: 4px 8px;
  border: 1px solid #eaeaea;
  border-radius: 5px;
  background: transparent;
  font-size: 12px;
  cursor: pointer;
}

.status-select:focus {
  outline: none;
  border-color: #7ca795;
}

.status-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  color: #aaa;
  font-size: 12px;
}

/* 加载和错误状态 */
.feedback {
  padding: 60px 15px;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.feedback p {
  margin-bottom: 12px;
}

.feedback button {
  padding: 8px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
}

/* 移动端适配 */
@media (max-width: 720px) {
  .workspace {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
    overflow: visible;
  }

  .sidebar {
    width: 100%;
    height: auto;
    padding: 18px 14px;
    border-right: none;
    border-bottom: 1px solid #eee;
    overflow: visible;
  }

  .nav-group {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 6px;
  }

  .nav-heading {
    width: 100%;
    margin-bottom: 6px;
  }

  .nav-item {
    width: auto;
    gap: 12px;
    padding: 9px 12px;
  }

  .main-content {
    overflow: visible;
  }

  .content-inner {
    padding: 32px 20px 60px;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .toolbar {
    flex-wrap: wrap;
  }

  .search-input {
    flex-basis: 100%;
  }

  .form-row {
    flex-direction: column;
  }

  .task-side {
    min-width: 75px;
  }
}
</style>
